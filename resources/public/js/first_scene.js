function render(ren, sce, cam) {
    var timer = Date.now() * 0.0001;

    cam.position.set(Math.cos(timer) * 200, 0, Math.sin(timer) * 200);
    cam.lookAt(sce.position);

    for (var i = 0, l = sce.children.length; i < l; i ++) {
	var object = sce.children[i];
	object.rotation.x += 0.01;
	object.rotation.y += 0.005;
    }
    ren.render(sce, cam);
}

function animate(f) {
    function go() {
        requestAnimationFrame(go);
        f();
    }
    go();
}
