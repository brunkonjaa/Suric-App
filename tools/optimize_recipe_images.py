#!/usr/bin/env python3
"""Re-encode registered recipe images to a strict per-file byte limit."""

from __future__ import annotations

import argparse
import io
import json
import math
from pathlib import Path

from PIL import Image, ImageCms


ROOT = Path(__file__).resolve().parents[1]
METADATA = ROOT / "app/src/main/assets/recipe-image-metadata.json"
IMAGE_ROOT = ROOT / "app/src/main/res/drawable-nodpi"
DEFAULT_LIMIT = 50_000
MAX_QUALITY = 87
MIN_QUALITY = 50
MAX_LONG_EDGE = 1280
MIN_LONG_EDGE = 320


def srgb_profile() -> bytes:
    return ImageCms.ImageCmsProfile(ImageCms.createProfile("sRGB")).tobytes()


def encode(image: Image.Image, quality: int, profile: bytes) -> bytes:
    buffer = io.BytesIO()
    image.save(buffer, format="WEBP", quality=quality, method=6, icc_profile=profile)
    return buffer.getvalue()


def best_fit(image: Image.Image, limit: int, profile: bytes) -> tuple[bytes, int, Image.Image]:
    current = image
    while True:
        low, high = MIN_QUALITY, MAX_QUALITY
        best: tuple[bytes, int] | None = None
        while low <= high:
            quality = (low + high) // 2
            payload = encode(current, quality, profile)
            if len(payload) <= limit:
                best = (payload, quality)
                low = quality + 1
            else:
                high = quality - 1
        if best:
            return best[0], best[1], current

        long_edge = max(current.size)
        if long_edge <= MIN_LONG_EDGE:
            raise RuntimeError(f"Cannot fit {current.size} image below {limit} bytes at quality {MIN_QUALITY}")
        min_quality_payload = encode(current, MIN_QUALITY, profile)
        scale = min(0.9, math.sqrt(limit / len(min_quality_payload)) * 0.97)
        scale = min(scale, (long_edge - 1) / long_edge)
        size = (max(1, round(current.width * scale)), max(1, round(current.height * scale)))
        current = current.resize(size, Image.Resampling.LANCZOS)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--limit-bytes", type=int, default=DEFAULT_LIMIT)
    parser.add_argument("--dry-run", action="store_true", help="Report selected encodings without writing files")
    args = parser.parse_args()

    metadata = json.loads(METADATA.read_text(encoding="utf-8"))
    profile = srgb_profile()
    report: list[tuple[str, int, int, int, int, int]] = []
    for item in metadata["images"]:
        path = IMAGE_ROOT / f"{item['assetName']}.webp"
        if not path.is_file():
            raise FileNotFoundError(path)
        original_bytes = path.stat().st_size
        with Image.open(path) as source:
            source.load()
            original_size = source.size
            embedded_profile = source.info.get("icc_profile")
            image = source.convert("RGB")
            if embedded_profile:
                image = ImageCms.profileToProfile(
                    image,
                    ImageCms.ImageCmsProfile(io.BytesIO(embedded_profile)),
                    ImageCms.ImageCmsProfile(io.BytesIO(profile)),
                    outputMode="RGB",
                )
        if max(image.size) > MAX_LONG_EDGE:
            scale = MAX_LONG_EDGE / max(image.size)
            size = (round(image.width * scale), round(image.height * scale))
            image = image.resize(size, Image.Resampling.LANCZOS)
        payload, quality, output_image = best_fit(image, args.limit_bytes, profile)
        report.append((item["assetName"], original_bytes, len(payload), quality, *output_image.size))
        if not args.dry_run:
            path.write_bytes(payload)
            item.setdefault("preCapBytes", original_bytes)
            item.setdefault("preCapDimensionsPx", {"width": original_size[0], "height": original_size[1]})
            item["dimensionsPx"] = {"width": output_image.width, "height": output_image.height}
            item["bytes"] = len(payload)
            item["encodingQuality"] = quality
            item["colorProfile"] = "Converted to and embedded as sRGB."
            item["byteCapBytes"] = args.limit_bytes
            item["compressionNote"] = (
                f"Re-encoded as static WebP with quality {quality}, preserving aspect ratio; "
                f"scaled only as needed to stay within {args.limit_bytes:,} bytes."
            )

    if not args.dry_run:
        standard = metadata["imageStandard"]
        standard["qualityPolicy"] = f"Use quality up to {MAX_QUALITY}; reduce adaptively only as needed to meet the per-image byte cap."
        standard["maximumBytesPerImage"] = args.limit_bytes
        standard["averageSizeTargetBytes"] = args.limit_bytes
        standard["minimumQualityBeforeResizing"] = MIN_QUALITY
        standard["minimumLongEdgePx"] = MIN_LONG_EDGE
        standard["standardMinimumLongEdgePx"] = None
        standard["standardMaximumLongEdgePx"] = MAX_LONG_EDGE
        standard["automaticResizingRequired"] = True
        METADATA.write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    for name, before, after, quality, width, height in report:
        print(f"{name}: {before} -> {after} bytes; q{quality}; {width}x{height}")
    print(f"TOTAL: {sum(row[1] for row in report)} -> {sum(row[2] for row in report)} bytes across {len(report)} images")
    print(f"MAX: {max(row[2] for row in report)} bytes")


if __name__ == "__main__":
    main()
