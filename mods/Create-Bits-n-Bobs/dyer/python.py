import os
from PIL import Image
import numpy as np
import colorsys
import math

# stop using chet gepetee smh
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

# Compute average HSL in the mask
def average_hsl_in_mask(base_img, mask_img):
    arr = np.array(base_img.convert('RGBA'))
    mask = np.array(mask_img.convert('L'))
    white_mask = mask >= 250
    h_list, s_list, l_list = [], [], [],
    for y, x in zip(*np.where(white_mask)):
        r, g, b, a = arr[y, x]
        h, l, s = colorsys.rgb_to_hls(r/255, g/255, b/255)
        h_list.append(h)
        s_list.append(s)
        l_list.append(l)
    if not h_list:
        return 0, 0, 0
    avg_h = sum(h_list) / len(h_list)
    avg_s = sum(s_list) / len(s_list)
    avg_l = sum(l_list) / len(l_list)
    return avg_h, avg_s, avg_l
    # forcedColor = "b63a3a"
    # forcedColor = forcedColor.lstrip('#')
    # r, g, b = (int(forcedColor[i:i+2], 16) for i in (0, 2, 4))
    # h, l, s = colorsys.rgb_to_hls(r/255, g/255, b/255)
    return h, s, l
    

# Apply color to white pixels only, shifting HSL to match target average
def colorize_image(base_img, mask_img, target_rgb):
    arr = np.array(base_img.convert('RGBA'))
    mask = np.array(mask_img.convert('L'))
    white_mask = mask >= 250
    # Compute average HSL in mask
    avg_h, avg_s, avg_l = average_hsl_in_mask(base_img, mask_img)
    # Target HSL
    th, tl, ts = colorsys.rgb_to_hls(*[c/255 for c in target_rgb])
    # Compute shift
    h_shift = th - avg_h
    s_shift = ts - avg_s
    l_shift = tl - avg_l
    # Store original alpha channel
    alpha_channel = arr[..., 3].copy()
    for y, x in zip(*np.where(white_mask)):
        r, g, b, a = arr[y, x]
        h, l, s = colorsys.rgb_to_hls(r/255, g/255, b/255)
        nh = (h + h_shift) % 1.0
        ns = min(max(s + s_shift, 0), 1)
        nl = min(max(l + l_shift, 0), 1)
        # Reduce contrast in brightness by blending with the average lightness
        # contrast_blend = max((0.8 - nl) * 0.3, 0) # 0 = no blend, 1 = full average, adjust as needed
        # nl = nl * (1 - contrast_blend) + avg_l * contrast_blend
        # USE THIS IF LIGHT
        nr, ng, nb = colorsys.hls_to_rgb(nh, nl * 1.2, ns)
        arr[y, x, 0:3] = [
            min(max(int(nr*255), 0), 255),
            min(max(int(ng*255), 0), 255),
            min(max(int(nb*255), 0), 255)
            ]
    # Restore original alpha channel for all pixels
    arr[..., 3] = alpha_channel
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
            for name, hex_code in colours:
                rgb = hex_to_rgb(hex_code)
                out_img = colorize_image(base_img, mask_img, rgb)
                out_name = f"{os.path.splitext(fname)[0]}_{name.replace(' ', '_').lower()}.png"
                out_img.save(os.path.join(out_dir, out_name))

if __name__ == '__main__':
    main()
