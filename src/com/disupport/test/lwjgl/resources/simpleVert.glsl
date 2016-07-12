#version 330

in vec3 position;
out float depth;

uniform mat4 viewMat;
uniform mat4 projectionMat;

void main() {
    gl_Position = projectionMat * viewMat * vec4(position, 1.0f);
    depth = (position.z + 1f) / 2f;
}
