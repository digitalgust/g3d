import org.mini.g3d.core.vector.Vector3f;

public class Test {
    public static void main(String[] args) throws Exception {
        float noise = 0.5f;
        float distance = 50;
        float farPlane = 500f;
        noise = (float) Math.pow(noise, 1.5);  // 增强高噪声区域的对比度（可选）
        Vector3f fogColor = new Vector3f(0.8f, 0.85f, 0.9f);  // 保持淡蓝色
        float fogDensity = 0.05f;  // 原0.3f→0.25f（降低基础密度，配合归一化后更平缓）
        float fogGradient = 1.0f;
        float normalizedDistance = distance / 500f;
        float fogFactor = normalizedDistance * fogDensity;  // 原distance*fogDensity → 归一化后更平缓

        float yAttenuation = (float) Math.max(0.0, 1.0 - 50 / farPlane);

        fogFactor = (float) Math.pow(fogFactor, 2.0);
        System.out.println("fogFactor2=" + fogFactor);
        fogFactor = fogFactor * ( noise );  // 降低噪声影响（原0.3→0.2，减少团雾强度）
        System.out.println("fogFactor3=" + fogFactor);
        fogFactor *= 5000.0;  // 全局缩放从0.5→0.4，进一步降低fogFactor
        System.out.println("fogFactor4=" + fogFactor);

        // 可见性计算（距离越远雾越浓）
        float visibility = (float) (Math.exp(-fogFactor));
        System.out.println("visibility=" + visibility);
    }
}