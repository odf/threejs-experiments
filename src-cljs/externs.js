var THREE = {};

THREE.Color                    = function() {};
THREE.Color.getHex             = function() {};
THREE.Color.setHex             = function() {};

THREE.Face3                    = function() {};
THREE.Face4                    = function() {};

THREE.Geometry                       = function() {};
THREE.Geometry.computeBoundingSphere = function() {};
THREE.Geometry.computeCentroids      = function() {};
THREE.Geometry.computeFaceNormals    = function() {};
THREE.Geometry.computeVertexNormals  = function() {};
THREE.Geometry.faces                 = {};
THREE.Geometry.vertices              = {};

/**
 * @constructor
 */
THREE.MeshLambertMaterial      = function() {};

/**
 * @constructor
 */
THREE.MeshPhongMaterial        = function() {};

/**
 * @constructor
 */
THREE.Mesh                     = function() {};

/**
 * @type {THREE.MeshLambertMaterial | THREE.MeshPhongMaterial}
 */
THREE.Mesh.material;

THREE.Object3D                 = function() {};

THREE.PerspectiveCamera        = function() {};
THREE.PerspectiveCamera.lookAt = function() {};

THREE.PointLight               = function() {};

THREE.Projector                = function() {};
THREE.Projector.unprojectVector = function() {};

/**
 * @constructor
 */
function IntersectResult() {};

/**
 * @type {THREE.Mesh}
 */
IntersectResult.object;

THREE.Ray                      = function() {};

/**
 * @return {Array.<IntersectResult>}
 */
THREE.Ray.intersectObjects     = function() {};

THREE.Scene                    = function() {};
THREE.Scene.getChildByName     = function() {};

THREE.SphereGeometry           = function() {};

THREE.Vector3                  = function() {};
THREE.Vector3.normalize        = function() {};
THREE.Vector3.subSelf          = function() {};

THREE.WebGLRenderer            = function() {};
THREE.WebGLRenderer.domElement = {};
THREE.WebGLRenderer.render     = function() {};
THREE.WebGLRenderer.setSize    = function() {};
