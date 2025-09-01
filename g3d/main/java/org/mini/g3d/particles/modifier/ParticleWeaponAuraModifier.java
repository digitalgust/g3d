package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;

/**
 * 武器光环修饰器
 * 实现螺旋流光效果，粒子围绕武器做螺旋运动
 * 支持多层螺旋和变化的半径
 */
public class ParticleWeaponAuraModifier extends ParticleModifier {

    float startAt;
    float endAt;
    float radius;           // 基础半径
    float radiusVariation;  // 半径变化幅度
    float height;           // 螺旋高度范围
    float spiralSpeed;      // 螺旋速度（转速）
    float verticalSpeed;    // 垂直移动速度
    Vector3f weaponCenter;  // 武器中心点
    float spiralLayers;     // 螺旋层数
    float particleOffset;   // 粒子间的相位偏移
    
    @Override
    public void update(Particle particle) {
        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();
        
        if (t >= startAt && t <= endAt) {
            float e = t - startAt;
            
            // 获取粒子的唯一标识符来创建不同的轨迹
            float particleId = (float) particle.hashCode() % 100 / 100.0f;
            
            // 计算螺旋参数
            float theta = (float) (spiralSpeed * e * 2 * Math.PI + particleId * particleOffset);
            float layerOffset = particleId * spiralLayers;
            
            // 计算当前半径（带有变化和层次）
            float currentRadius = radius + 
                (float) (radiusVariation * Math.sin(e * 3 + layerOffset)) +
                layerOffset * 0.3f;
            
            // 计算垂直位置（螺旋上升/下降）
            float verticalPos = (float) (height * Math.sin(verticalSpeed * e + layerOffset));
            
            // 获取武器中心位置（如果没有设置则使用发射器位置）
            Vector3f center = weaponCenter != null ? weaponCenter : particle.getEmitter().getEmitLocation();
            
            // 计算粒子在螺旋轨迹上的位置
            float x = (float) (center.x + currentRadius * Math.cos(theta));
            float y = center.y + verticalPos;
            float z = (float) (center.z + currentRadius * Math.sin(theta));
            
            // 添加重力影响
            y += particle.getGravityY();
            
            particle.getPosition().set(x, y, z);
        }
    }

    /**
     * ======================================================
     * setter methods
     * ======================================================
     */
    
    public void setStartAt(float startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(float endAt) {
        this.endAt = endAt;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setRadiusVariation(float radiusVariation) {
        this.radiusVariation = radiusVariation;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setSpiralSpeed(float spiralSpeed) {
        this.spiralSpeed = spiralSpeed;
    }

    public void setVerticalSpeed(float verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    public void setWeaponCenter(Vector3f weaponCenter) {
        this.weaponCenter = weaponCenter;
    }

    public void setSpiralLayers(float spiralLayers) {
        this.spiralLayers = spiralLayers;
    }

    public void setParticleOffset(float particleOffset) {
        this.particleOffset = particleOffset;
    }
}