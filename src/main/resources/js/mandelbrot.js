function mandelbrot(x, y, z0x, z0y, iterations) {
    let zx = z0x;
    let zy = z0y;
    let z2x = zx * zx;
    let z2y = zy * zy;
    let r = [{x : x, y : y}];

    while (z2x + z2y <= 4 && r.length < iterations) {
        zy = 2 * zx * zy + y;
        zx = z2x - z2y + x;
        z2x = zx * zx;
        z2y = zy * zy;

        r.push({x : zx, y : zy});
    }

    return r;
}

function julia(x, y, z0x, z0y, iterations) {
    let zx = x;
    let zy = y;
    let z2x = zx * zx;
    let z2y = zy * zy;
    let r = [{x : x, y : y}];

    while (z2x + z2y <= 4 && r.length < iterations) {
        zy = 2 * zx * zy + z0y;
        zx = z2x - z2y + z0x;
        z2x = zx * zx;
        z2y = zy * zy;

        r.push({x : zx, y : zy});
    }

    return r;
}

