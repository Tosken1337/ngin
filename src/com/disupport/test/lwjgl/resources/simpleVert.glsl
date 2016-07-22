#version 330

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;

out vec2 texCoordOut;


uniform mat4 viewMat;
uniform mat4 projectionMat;

void main() {
    gl_Position = projectionMat * viewMat * vec4(position, 1.0f);
    texCoordOut = texCoord;
}
