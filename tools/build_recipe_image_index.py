from __future__ import annotations
import json
import re
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app' / 'src' / 'main' / 'assets'
OUTPUT = ROOT / 'COOKING_DISH_IMAGE_INDEX.md'


def cell(value: str) -> str:
    return re.sub(r'\s+', ' ', str(value)).replace('|', r'\|').strip()


def main() -> None:
    manifest = json.loads((ASSETS / 'recipe-catalogue-manifest.json').read_text(encoding='utf-8-sig'))
    files = ['recipe-catalogue.json', *manifest['catalogues']]
    recipes = {}
    memberships: dict[str, list[str]] = defaultdict(list)
    for filename in files:
        catalogue = json.loads((ASSETS / filename).read_text(encoding='utf-8-sig'))
        for recipe in catalogue.get('recipes', []):
            recipe_id = recipe['id']
            if recipe_id in recipes:
                raise ValueError(f'Duplicate canonical recipe ID: {recipe_id}')
            recipes[recipe_id] = recipe
        for collection in catalogue.get('collections', []):
            for recipe_id in collection.get('recipeIds', []):
                memberships[recipe_id].append(collection['title'])

    unknown_members = set(memberships) - recipes.keys()
    if unknown_members:
        raise ValueError(f'Collection references unknown recipes: {sorted(unknown_members)[:5]}')

    rows = []
    for recipe_id, recipe in recipes.items():
        collections = sorted(set(memberships.get(recipe_id, [])), key=str.casefold)
        food_words = ' '.join([recipe.get('name', ''), recipe.get('englishName') or '', *recipe.get('tags', [])]).casefold()
        bbq_collection = any('bbq' in title.casefold() or 'barbecue' in title.casefold() for title in collections)
        fire_preparation = bbq_collection or any(word in food_words for word in ('grill', 'barbecue', 'bbq', 'smok', 'asado', 'parrilla'))
        origin = ', '.join(filter(None, [recipe.get('country', ''), recipe.get('region', '')]))
        dish = recipe.get('englishName') or recipe.get('name') or recipe_id
        scene = (
            'Show the finished dish served on a plate or table; show this recipe cooking over fire only when it fits its method.'
            if fire_preparation else
            'Show the finished dish as traditionally served, plated or set on a table; make the food the clear subject.'
        )
        query = f'{dish}, {origin}, finished dish served'
        if fire_preparation:
            query += '; include a grill, smoke or fire only when it matches the recipe'
        image_ids = recipe.get('imageAssets', [])
        for image_id in image_ids:
            image_path = ROOT / 'app' / 'src' / 'main' / 'res' / 'drawable-nodpi' / f'{image_id}.webp'
            if not image_path.is_file():
                raise FileNotFoundError(f'{recipe_id} references missing bundled image {image_path.relative_to(ROOT)}')
        rows.append((recipe.get('country', ''), dish, recipe_id, origin, collections, query, scene, image_ids))

    rows.sort(key=lambda row: (row[0].casefold(), row[1].casefold(), row[2]))
    total_images = sum(len(row[7]) for row in rows)
    lines = [
        '# Cooking dish image index',
        '',
        f'Generated from the 25 catalogue files registered in `app/src/main/assets/recipe-catalogue-manifest.json`. It contains **{len(rows)} unique canonical recipes** and **{sum(len(v) for v in memberships.values())} collection placements**. Recipe IDs are stable attachment keys; collection covers are excluded.',
        '',
        '## Image sourcing rules',
        '',
        '- Each row provides a search query and visual direction for one recipe-specific finished-dish image. Search for the actual dish and origin; prefer a clear plate or table presentation. Include fire, grill or smoke only when that recipe is actually grilled or smoked.',
        '- This index is a first-image discovery input. The project target remains five distinct recipe-specific images for each canonical recipe: **4,455 images**. An image counts only when bundled locally and attached to its canonical ID.',
        '- Accept an image only after verifying a redistributable license (public domain, CC0, or a suitable Creative Commons license), creator, original source page and attribution terms. Record those fields per image in `app/src/main/assets/recipe-image-metadata.json`. Do not rely on a direct image URL or on “free to download” alone.',
        '- Existing attachments are listed by Android drawable resource name. They are local WebP files; collection-cover assets do not appear in this column.',
        '- Image standard: lossy WebP, quality 87, sRGB, preserved aspect ratio, normally 960–1280 px on the long edge (up to 1600 px for instructional scenes), with no animation and transparency only when needed. New images must be resized automatically and pass duplicate detection before acceptance.',
        '- Aim for an average of 50 KB or less when the image remains clean and clear on a phone screen. Treat 250 MB as the rounded full-set storage budget; actual byte totals take precedence over estimates.',
        '',
        f'Current recipe-image inventory: **{total_images} / 4,455** images attached across **{sum(bool(row[7]) for row in rows)} / {len(rows)}** recipes; **{len(rows) * 5 - total_images}** images remain to the five-per-recipe target. Per-image bytes and source, creator, license and attribution are recorded in `app/src/main/assets/recipe-image-metadata.json`.',
        '',
        '| Recipe ID | Dish | Origin | Collections | Image search query | Image direction | Attached images |',
        '| --- | --- | --- | --- | --- | --- | --- |',
    ]
    for _, dish, recipe_id, origin, collections, query, scene, image_ids in rows:
        attached = ', '.join(f'`{image_id}`' for image_id in image_ids) if image_ids else '—'
        lines.append('| ' + ' | '.join(map(cell, [f'`{recipe_id}`', dish, origin, ', '.join(collections) or 'Unassigned', query, scene, attached])) + ' |')
    OUTPUT.write_text('\n'.join(lines) + '\n', encoding='utf-8')
    print(f'Wrote {OUTPUT.relative_to(ROOT)}: {len(rows)} recipes, {sum(len(v) for v in memberships.values())} collection placements, {total_images} attached image refs.')

if __name__ == '__main__':
    main()
