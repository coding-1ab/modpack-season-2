package com.kipti.bnb.content.girder_strut;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GirderStrutModelManipulatorTest {

    @Test
    void transformPlaneAppliesPoseAndNormalMatrix() {
        Matrix4f pose = new Matrix4f()
            .identity()
            .translate(1.25f, -0.5f, 2.75f)
            .rotateY((float) Math.toRadians(90));
        Matrix3f normalMatrix = new Matrix3f(pose);

        Vector3f planePoint = new Vector3f(0.25f, 0.5f, -0.125f);
        Vector3f planeNormal = new Vector3f(0f, 0f, 1f);

        GirderStrutModelManipulator.Plane transformed = GirderStrutModelManipulator.transformPlane(
            pose,
            normalMatrix,
            planePoint,
            planeNormal
        );

        Vector3f expectedPoint = new Vector3f(planePoint);
        pose.transformPosition(expectedPoint);
        Vector3f expectedNormal = new Vector3f(planeNormal);
        normalMatrix.transform(expectedNormal);
        expectedNormal.normalize();

        assertNotSame(planePoint, transformed.point());
        assertNotSame(planeNormal, transformed.normal());
        assertEquals(0.25f, planePoint.x, 1.0e-6f);
        assertEquals(0.5f, planePoint.y, 1.0e-6f);
        assertEquals(-0.125f, planePoint.z, 1.0e-6f);
        assertEquals(0f, planeNormal.x, 1.0e-6f);
        assertEquals(0f, planeNormal.y, 1.0e-6f);
        assertEquals(1f, planeNormal.z, 1.0e-6f);
        assertEquals(expectedPoint.x, transformed.point().x, 1.0e-6f);
        assertEquals(expectedPoint.y, transformed.point().y, 1.0e-6f);
        assertEquals(expectedPoint.z, transformed.point().z, 1.0e-6f);
        assertEquals(expectedNormal.x, transformed.normal().x, 1.0e-6f);
        assertEquals(expectedNormal.y, transformed.normal().y, 1.0e-6f);
        assertEquals(expectedNormal.z, transformed.normal().z, 1.0e-6f);
        assertTrue(Math.abs(transformed.normal().length() - 1f) < 1.0e-6f, "normal should be normalized");
    }
}
