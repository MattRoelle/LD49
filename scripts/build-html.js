const fs = require("fs");
const path = require("path");

const animations = fs.readdirSync("./lottie-animations");
const tpl = fs.readFileSync(path.join(__dirname, "./template.html")).toString();

const animationOutput = {};
for(let a of animations) {
    const fn = path.join("./lottie-animations", a);
    const f = fs.readFileSync(fn);
    animationOutput[a.replace(".json", "")] = JSON.parse(f.toString());
}

const html = tpl.replace("%ANIMATIONS%", JSON.stringify(animationOutput));
fs.writeFileSync("./public/index.html", html);