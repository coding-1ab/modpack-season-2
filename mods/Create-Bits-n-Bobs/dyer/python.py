import os
from PIL import Image
import numpy as np
import colorsys

# Parse colours.txt to get color names and hex codes
def parse_colours(colours_path):
    colours = []
    with open(colours_path, 'r', encoding='utf-8') as f:
        lines = [line.strip() for line in f if line.strip()]
        for i in range(0, len(lines), 2):
            name = lines[i].split('\t')[0]
            hex_code = lines[i+1].strip().lstrip('#')
            colours.append((name, hex_code))
    return colours

# Apply color to white pixels only
def colorize_image(base_img, mask_img, target_rgb, orig_rgb=None):
    arr = np.array(base_img.convert('RGBA'))
    mask = np.array(mask_img.convert('L'))
    # Only apply where mask is white (value >= 250)
    white_mask = mask >= 250
    target_rgb_f = np.array(target_rgb) / 255.0
    # Compute luminance and saturation of the target color
    target_lum = np.dot(target_rgb_f, [0.299, 0.587, 0.114])
    maxc = np.max(target_rgb_f)
    minc = np.min(target_rgb_f)
    target_s = 0 if maxc == 0 else (maxc - minc) / maxc
    # Convert input to grayscale (luminance)
    gray = np.dot(arr[...,:3], [0.299, 0.587, 0.114]) / 255.0
    for y, x in zip(*np.where(white_mask)):
        r, g, b, a = arr[y, x]
        # Convert original pixel to HLS
        h, l, s = colorsys.rgb_to_hls(r/255, g/255, b/255)
        # Convert target color to HLS
        th, tl, ts = colorsys.rgb_to_hls(*[c/255 for c in target_rgb])

        oh, ol, os = colorsys.rgb_to_hls(*[c/255 for c in orig_rgb]) if orig_rgb else (h, l, s)
        l_shift = target_lum - ol
        # For very low-saturation target colors, keep original lightness
        if ts < 0.05:
            new_l = l + l_shift
        elif ts < 0.2:
            # Blend original and target lightness, but favor original more
            blend = (ts - 0.05) / 0.15  # 0 at ts=0.05, 1 at ts=0.2
            new_l = l * (1 - blend) + tl * blend
        else:
            new_l = l
            # ts *= 1.5 + l  # Boost saturation for more vibrant colors
        # Replace hue and saturation, use new lightness
        nr, ng, nb = colorsys.hls_to_rgb(th, new_l, ts)
        rgb_clamped = [max(0, min(255, int(nr*255))),
                       max(0, min(255, int(ng*255))),
                       max(0, min(255, int(nb*255)))]
        arr[y, x, 0:3] = rgb_clamped
        arr[y, x, 3] = a  # Explicitly preserve alpha
    return Image.fromarray(arr)

def hex_to_rgb(hex_code):

    return tuple(int(hex_code[i:i+2], 16) for i in (0, 2, 4))

def main():
    in_dir = 'in'
    mask_path = 'mask.png'
    colours_path = 'colours.txt'
    out_dir = 'out'

    os.makedirs(out_dir, exist_ok=True)

    mask_img = Image.open(mask_path)
    colours = parse_colours(colours_path)

    for fname in os.listdir(in_dir):
        if fname.lower().endswith('.png'):
            base_img = Image.open(os.path.join(in_dir, fname)).convert('RGBA')
            # Set orig_rgb based on filename
            # orig_rgb is a reference color for the original image
            orig_rgb = (182,58,58)
            for name, hex_code in colours:
                rgb = hex_to_rgb(hex_code)
                out_img = colorize_image(base_img, mask_img, rgb, orig_rgb=orig_rgb)
                out_name = f"{os.path.splitext(fname)[0]}_{name.replace(' ', '_').lower()}.png"
                out_img.save(os.path.join(out_dir, out_name))

if __name__ == '__main__':
    main()
