import os
from PIL import Image

source_img_path = "/Users/irmo/.gemini/antigravity/brain/b7ba0f28-e5d6-4fd1-b1ac-e3e43fcbb490/app_icon_pushup_1771896984543.png"
res_dir = "/Volumes/Extreme SSD/Android/PumpItUp/app/src/main/res"

fg_sizes = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}

legacy_sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

try:
    with Image.open(source_img_path) as img:
        img = img.convert("RGBA")
        
        for folder, total_size in fg_sizes.items():
            target_dir = os.path.join(res_dir, folder)
            os.makedirs(target_dir, exist_ok=True)
            
            fg = Image.new("RGBA", (total_size, total_size), (0, 0, 0, 0))
            # Safe zone diameter is 72, which is 2/3 of 108.
            # We scale the icon to 0.65 of the total size to ensure it fits the safe zone tightly but safely.
            icon_size = int(total_size * 0.65)
            resized = img.resize((icon_size, icon_size), Image.Resampling.LANCZOS)
            
            offset = (total_size - icon_size) // 2
            fg.paste(resized, (offset, offset), resized)
            fg.save(os.path.join(target_dir, "ic_launcher_foreground.png"))
            print(f"Saved adaptive foreground {total_size}x{total_size} to {folder}")

        for folder, size in legacy_sizes.items():
            target_dir = os.path.join(res_dir, folder)
            os.makedirs(target_dir, exist_ok=True)
            
            resized = img.resize((size, size), Image.Resampling.LANCZOS)
            resized.save(os.path.join(target_dir, "ic_launcher.png"))
            resized.save(os.path.join(target_dir, "ic_launcher_round.png"))
            print(f"Saved legacy {size}x{size} to {folder}")
            
except Exception as e:
    print(f"Error: {e}")
