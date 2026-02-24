from PIL import Image
import os

sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

source_img_path = "/Users/irmo/.gemini/antigravity/brain/b7ba0f28-e5d6-4fd1-b1ac-e3e43fcbb490/app_icon_pushup_1771896984543.png"
res_dir = "/Volumes/Extreme SSD/Android/PumpItUp/app/src/main/res"

try:
    with Image.open(source_img_path) as img:
        # Convert to RGBA to ensure it works well
        img = img.convert("RGBA")
        
        for folder, size in sizes.items():
            target_dir = os.path.join(res_dir, folder)
            os.makedirs(target_dir, exist_ok=True)
            
            # The original icon is ic_launcher.webp or ic_launcher.png. 
            # We'll save as ic_launcher.png and ic_launcher_round.png.
            resized = img.resize((size, size), Image.Resampling.LANCZOS)
            
            resized.save(os.path.join(target_dir, "ic_launcher.png"))
            resized.save(os.path.join(target_dir, "ic_launcher_round.png"))
            print(f"Saved {size}x{size} to {folder}")
            
except Exception as e:
    print(f"Error: {e}")
