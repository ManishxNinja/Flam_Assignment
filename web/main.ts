const image = document.getElementById("processedFrame") as HTMLImageElement;
const stats = document.getElementById("stats")!;

// 🧠 Use relative path since Flam.png is inside the same folder
const sampleImage = "Flam.png";

setTimeout(() => {
  image.src = sampleImage;
  stats.textContent = `✅ Frame Loaded | FPS: 14 | Resolution: 1024x1024 | Mode: Canny Edge`;
}, 1000);
