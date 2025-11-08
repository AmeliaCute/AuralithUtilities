#version 150

uniform float GameTime;

in vec3 worldPos;

out vec4 fragColor;

void main()
{
    // Distance from center (0,0,0)
    float dist = length(worldPos);
    float time = GameTime * 2400.0; // Speed up animation

    // Animated procedural waves
    float wave1 = sin(worldPos.x * 5.0 + time * 0.5) * 0.5 + 0.5;
    float wave2 = sin(worldPos.y * 5.0 + time * 0.7) * 0.5 + 0.5;
    float wave3 = sin(worldPos.z * 5.0 + time * 0.3) * 0.5 + 0.5;
    float waves = (wave1 + wave2 + wave3) / 3.0;

    // Fresnel effect for edge glow
    float fresnel = 1.0 - abs(normalize(worldPos).z);
    fresnel = pow(fresnel, 2.0);

    // Energy core effect
    float core = 1.0 - smoothstep(0.0, 0.5, dist);

    // Combine waves, fresnel, and core
    float energy = mix(waves, 1.0, fresnel * 0.5) * (0.7 + core * 0.3);

    // Color cycling
    vec3 color1 = vec3(0.3, 0.6, 1.0); // Cyan
    vec3 color2 = vec3(0.8, 0.3, 1.0); // Purple
    vec3 finalColor = mix(color1, color2, waves);

    // Apply energy intensity
    finalColor *= energy * 1.5;

    // Remove vertex color blending, ensure black background
    fragColor = vec4(finalColor, 1.0); // Fully opaque
}
