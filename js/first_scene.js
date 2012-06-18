function light(position, color)
{
    var l = new THREE.PointLight(color);
    l.position.set(position.x, position.y, position.z);
    return l;
}

function lambert(color)
{
    return new THREE.MeshLambertMaterial({ color: color });
}

function sphere(radius, segments, rings)
{
    return new THREE.SphereGeometry(radius, segments, rings);
}

function mesh(geometrie, position, material)
{
    var m = new THREE.Mesh(geometrie, material);
    m.position.set(position.x, position.y, position.z);
    return m;
}

function scene()
{
    var scene = new THREE.Scene();

    scene.add(mesh(sphere(50, 16, 16), { x: 0, y: 0, z: 0 },
                   lambert(0xCC2020)));
    scene.add(mesh(sphere(20, 16, 16), { x: 80, y: 50, z: 0 },
                   lambert(0xCCCCCC)));

    scene.add(light({ x: 150, y: 300, z: 1000 }, 0xFFFFFF));
    scene.add(light({ x: -150, y: 300, z: -1000 }, 0x8080FF));

    return scene;
}

function camera(viewport)
{
    var view_angle = 45;
    var aspect = viewport.width / viewport.height;
    var near = 0.1;
    var far = 10000;

    return new THREE.PerspectiveCamera(view_angle, aspect, near, far);
}

function renderer(viewport)
{
    var ren = new THREE.WebGLRenderer();
    ren.setSize(viewport.width, viewport.height);
    return ren;
}

function attach(ren, container)
{
    container.append(ren.domElement);
}

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
