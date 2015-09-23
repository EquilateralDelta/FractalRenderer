uniform vec2 resolution;
uniform vec3 cameraPosition;

vec4 distanceUnion(vec4 d1, vec4 d2) {
    return (d1.x < d2.x) ? d1 : d2;
}

vec4 mandelbulb(vec3 p) {
    p.xyz = p.xzy;

    vec3 z = p;
    vec3 dz = vec3(0.0);
    float power = 8.0;
    float r, theta, phi;
    float dr = 1.0;

    float trap = 1.0;

    for(int i = 0; i < 7; i++) {
        r = length(z);

        if(r > 2.0)
            continue;

        theta = atan(z.y / z.x);
        phi = asin(z.z / r);

        dr = pow(r, power - 1.0) * dr * power + 1.0;

        r = pow(r, power);
        theta = theta * power;
        phi = phi * power;

        z = r * vec3(cos(theta) * cos(phi), sin(theta) * cos(phi), sin(phi)) + p;

        trap = min(trap, r);
    }

    trap = pow(clamp(trap, 0.0, 1.0), 0.55);
    vec3 trapColor = 0.5 + 0.5 * sin(3.0 + 4.2 * trap + vec3(0.0, 0.5, 1.0));
    return vec4(0.5 * log(r) * r / dr, trapColor);
}

vec4 map(vec3 p) {
    vec4 result = mandelbulb(p);
    return result;
}

float shadow(vec3 origin, vec3 direction, float minDist, float maxDist, float k) {
    const int maxIt = 64;

    float result = 1.0;
    float distance = minDist;

    for (int i = 0; i < maxIt; i++) {
        float h = map(origin + direction * distance).x;

        result = min(result, k * h / distance);
        distance += clamp(h, minDist, 0.1);

        if (h < 0.0001 || distance > maxDist)
            break;
    }

    return clamp(result, 0.0, 1.0);
}

vec3 calcNormal(vec3 p) {
    vec3 eps = vec3(0.001, 0.0, 0.0);
    vec3 normal = vec3(map(p + eps.xyy).x - map(p - eps.xyy).x,
                       map(p + eps.yxy).x - map(p - eps.yxy).x,
                       map(p + eps.yyx).x - map(p - eps.yyx).x);

    return normalize(normal);
}

float calcAmbientOcclusion(vec3 p, vec3 normal) {
    const int maxIt = 5;

    float occlusion = 0.0;
    float scale = 1.0;

    for (int i = 0; i < maxIt; i++) {
        float hr = 0.01 + 0.12 * float(i) / 4.0;
        vec3 aoPos = normal * hr + p;
        float dd = map(aoPos).x;
        occlusion += -(dd - hr) * scale;
        scale *= 0.95;
    }

    return clamp(1.0 - 3.0 * occlusion, 0.0, 1.0);
}

vec3 lightDirection;

vec3 lighting(vec3 p, vec3 direction) {
    vec3 normal = calcNormal(p);
    vec3 reflection = reflect(direction, normal);

    vec3 diffuseColor = vec3(1.0, 0.9, 0.8);
    vec3 specularColor = diffuseColor;
    vec3 ambientColor = vec3(0.5, 0.7, 1.0);
    vec3 skyColor = ambientColor;
    vec3 backColor = vec3(0.25, 0.25, 0.25);
    vec3 fresnelColor = vec3(1, 1, 1);

    float occlusion = calcAmbientOcclusion(p, normal);
    float ambient = clamp(0.5 + 0.5 * normal.y, 0.0, 1.0);
    float diffuse = clamp(dot(normal, lightDirection), 0.0, 1.0);
    float back = clamp(dot(normal, normalize(vec3(-lightDirection.x, 0.0, -lightDirection.z))), 0.0, 1.0) * clamp(1.0 - p.y, 0.0, 1.0);
    float sky = smoothstep(-0.1, 0.1, .2);//reflection.y);
    float fresnel = pow(clamp(1.0 + dot(normal, direction), 0.0, 1.0), 2.0);
    float specular = pow(clamp(dot(reflection, lightDirection), 0.0, 1.0), 16.0);

    diffuse *= shadow(p, lightDirection, 0.02, 2.5, 8);
    sky *= shadow(p, reflection, 0.02, 2.5, 8);

    vec3 color = vec3(0.0);
    color += 1.20 * diffuse * diffuseColor;
    color += 1.20 * specular * specularColor * diffuse;
    color += 0.30 * ambient * ambientColor * occlusion;
    color += 0.40 * sky * skyColor * occlusion;
    color += 0.30 * back * backColor * occlusion;
    color += 0.40 * fresnel * fresnelColor * occlusion;

    return color;
}

vec4 castRay(vec3 origin, vec3 direction) {
    const float minDist = 0.0;
    float maxDist = 100.0;
    const int maxIt = 100;
    const float precis = 0.001;

    float distance = minDist;
    vec3 color = vec3(-1.0);

    for (int i = 0; i < maxIt; i++) {
        vec4 result = map(origin + direction * distance);

        if (result.x < precis || distance > maxDist)
            break;

        distance += result.x;
        color = result.yzw;
    }

    if (distance > maxDist)
        color = vec3(-1.0);

    return vec4(distance, color);
}

mat3 GetCam(vec3 pos, vec3 target)
{
    vec3 z = normalize(target - pos);
    vec3 up = vec3(0, 1, 0);
    vec3 x = normalize(cross(z, up));
    vec3 y = normalize(cross(x, z));

    return mat3(x, y, z);
}

vec3 render(vec3 origin, vec3 direction) {
    const vec3 background = vec3(0.5, 1, 1);

    vec4 result = castRay(origin, direction);
    float distance = result.x;
    vec3 color = result.yzw;

    if (color.x >= -10e-4) {
        vec3 position = origin + distance * direction;
        color *= lighting(position, direction);
        color = mix(color, background, 1.0 - exp(-0.0075 * distance * distance));
    } else color = background;

    return clamp(color, 0.0, 1.0);
}

void main(){
    float PI = acos(0) * 2;

    vec2 pos = 2 * gl_FragCoord.xy / resolution - 1;
    pos.x *= resolution.x / resolution.y;

    mat3 cam = GetCam(cameraPosition, vec3(0, 0, 0));

    lightDirection = normalize(cam * normalize(vec3(-0.5, 0.5, -0.5)));

    vec3 ray = cam * normalize(vec3(pos, 2.5));
    vec3 color = render(cameraPosition, ray);

    color = pow(color, 1.0 / 1.5);

    gl_FragColor = vec4(color, 1);
}
