const fillColor = '#16181d';
const circleColor = '#c98b6b';
const fractalColor = '#6f737a'
const pathColor = '#509be0';

window.addEventListener('load', function () {
    let fractal = "mandelbrot";
    let z0x = 0;
    let z0y = 0
    let iterations = 10;

    const canvas = document.getElementById('canvas');
    const background = document.createElement('canvas');

    const width = canvas.clientWidth;
    const height = canvas.clientHeight;
    const zoom = 4 / (Math.min(height, width) * 0.8); //60% of height = 4x4 square
    const offsetX = -width * zoom / 2;
    const offsetY = -height * zoom / 2;
    const minx = sx(-2);
    const maxx = sx(2);
    const miny = sy(-2);
    const maxy = sy(2);

    canvas.setAttribute('width', width + '');
    canvas.setAttribute('height', height + '');
    background.setAttribute('width', width + '');
    background.setAttribute('height', height + '');

    const ctx = canvas.getContext("2d");

    drawBackground();
    ctx.drawImage(background, 0, 0);

    canvas.addEventListener('mousemove', (event) => drawPath(event.offsetX, event.offsetY));

    document.getElementById('fractalInput').onchange = event => {
        fractal = event.target.value;
        drawBackground();
        ctx.drawImage(background, 0, 0);
    }

    document.getElementById('zx').oninput = event => {
        z0x = parseInt(event.target.value) / 10;
        drawBackground();
        ctx.drawImage(background, 0, 0);
    }

    document.getElementById('zy').oninput = event => {
        z0y = parseInt(event.target.value) / 10;
        drawBackground();
        ctx.drawImage(background, 0, 0);
    }

    document.getElementById('it').oninput = event => {
        iterations = parseInt(event.target.value);
        drawBackground();
    }

    //screen to world x
    function wx(x) {
        return x * zoom + offsetX;
    }

    //screen to world y
    function wy(y) {
        return y * zoom + offsetY;
    }

    //world to screen x
    function sx(x) {
        return (x - offsetX) / zoom;
    }

    //world to screen y
    function sy(y) {
        return (y - offsetY) / zoom;
    }

    function drawBackground() {
        const ctx = background.getContext("2d");

        ctx.fillStyle = fillColor;
        ctx.fillRect(0, 0, width, height);

        ctx.beginPath();
        ctx.strokeStyle = circleColor;
        ctx.lineWidth = 3;
        ctx.arc(sx(0), sy(0), 2 / zoom, 0, 2 * Math.PI);
        ctx.stroke();

        const fractal = getFractal();

        ctx.fillStyle = fractalColor;

        for (let x = minx; x <= maxx; x++) {
            for (let y = miny; y <= maxy; ++y) {
                const path = fractal(wx(x), wy(y), z0x, z0y, 30);
                if (path.length === 30) {
                    ctx.fillRect(x, y, 1, 1);
                }
            }
        }
    }

    function drawPath(x, y) {
        ctx.drawImage(background, 0, 0);

        const path = getFractal()(wx(x), wy(y), z0x, z0y, iterations);

        if (path.length > 1) {
            ctx.strokeStyle = pathColor;
            ctx.lineWidth = 3;

            ctx.beginPath();
            ctx.moveTo(sx(path[0].x), sy(path[0].y));

            for (let i = 1; i < path.length; ++i) {
                ctx.lineTo(sx(path[i].x), sy(path[i].y))
            }

            ctx.stroke();
        }
    }

    function getFractal() {
        return fractal.toLowerCase() === "mandelbrot" ? mandelbrot : julia;
    }
})
