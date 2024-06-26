package org.firstinspires.ftc.teamcode.Mechanisms;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDCoefficients;

import org.firstinspires.ftc.robotcontroller.Control.PID_Controller;
import org.firstinspires.ftc.robotcontroller.Hardware.Arms.MotorArm;
import org.firstinspires.ftc.robotcontroller.Hardware.Lifts.Slides;
import org.firstinspires.ftc.robotcontroller.Hardware.Motor;
import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class TenacitySlides extends Slides {
    private Gamepad gamepad1;
    private Telemetry telemetry;

    private final static String[] name = {"slidesLeft", "slidesRight"};
    private final static double pulleyRadius = 0.57 * 1.5 * ((double) 16 / 46); //16/46

    public static PIDCoefficients SLIDE_PID_COEFFS = new PIDCoefficients(0.9, 0, 0);
    public static double SLIDE_KGRAVITY = 0;
    public static double ARM_ADJUST = 0.015;

    public enum SlidesState {
        INIT,
        FAR_INTAKE,
        CLOSE_INTAKE,
        FIRST_HALF,
        FIRST_LINE,
        THIRD_HALF,
        SECOND_LINE,
        FIFTH_HALF,
        THIRD_LINE,
        HANG_ALIGN,
        HANG_PULL,
        MANUAL
    }

    public static double FIRST_HALF = 1;
    public static double FIRST_LINE = 1.1;
    public static double THIRD_HALF = 2.2;
    public static double SECOND_LINE = 3.3;
    public static double FIFTH_HALF = 4.4;
    public static double THIRD_LINE = 5.5;
    public static double HANG_ALIGN = 5.5;

    public SlidesState slidesState = SlidesState.INIT;
    private PID_Controller slidesPID = new PID_Controller(SLIDE_PID_COEFFS, 0);

    public MotorArm arm;

    private double slidesPower = 0;
    private double targetPos = 0;

    public TenacitySlides(MotorArm arm, Gamepad gamepad1, Telemetry telemetry, HardwareMap hardwareMap) {
        super(2, name, pulleyRadius, StringingMethod.CASCADE, 2, SLIDE_KGRAVITY, hardwareMap); //0.175

        slidesPID.tolerance = 0.001;

        this.arm = arm;

        motors[0].setBrakeMode();
        motors[1].setBrakeMode();

        motors[0].setDirectionReverse();
        motors[1].setDirectionReverse();

        this.gamepad1 = gamepad1;
        this.telemetry = telemetry;
    }

    @Override
    public void setPower(double power) {
        for (Motor motor : motors)
            motor.setPower(SLIDE_KGRAVITY * Math.sin(Math.toRadians(arm.getAngleDegrees())) + power);
    }

    public void resetEncoders() {
        motors[0].reset();
        motors[1].reset();
    }

    // Method looped to continually set power to slides based on state
    public void setSlidePower() {
        switch (slidesState) {
            case INIT:
                targetPos = 1;
                break;
            case FAR_INTAKE:
                targetPos = 6;
                break;
            case FIRST_HALF:
                targetPos = FIRST_HALF;
                break;
            case FIRST_LINE:
                targetPos = FIRST_LINE;
                break;
            case THIRD_HALF:
                targetPos = THIRD_HALF;
                break;
            case SECOND_LINE:
                targetPos = SECOND_LINE;
                break;
            case FIFTH_HALF:
                targetPos = FIFTH_HALF;
                break;
            case THIRD_LINE:
                targetPos = THIRD_LINE;
                break;
            case HANG_ALIGN:
                targetPos = HANG_ALIGN;
                break;
            case HANG_PULL:
                slidesPower = -1;
                break;
            case MANUAL:
                slidesPower = gamepad1.right_trigger - gamepad1.left_trigger;
                break;
        }

        if (slidesState != SlidesState.MANUAL && slidesState != SlidesState.HANG_PULL)
            slidesPower = slidesPID.PID_Power(getExtension(), targetPos);
        setPower(slidesPower);
    }

    public double getExtension() {
        if (method == StringingMethod.CONTINUOUS) {
            height = Math.toRadians(motors[0].getCurrPosDegrees()) * pulleyRadius;
        } else if (method == StringingMethod.CASCADE) {
            height = stages * (motors[0].getCurrPosRadians() * pulleyRadius) - (arm.getAngleDegrees() - 90) * ARM_ADJUST;
        }

        return height;
    }

    // Sends telemetry data for slides to a queue to be shown on driver station after telemetry.update() is called
    public void slidesTelemetry() {
        telemetry.addData("Slides Height", getExtension());
        telemetry.addData("Slide State", slidesState);
        telemetry.addData("Error", slidesPID.error);
        telemetry.addData("PID Power", slidesPower);
    }

    public void TuningTelemetry() {
        telemetry.addData("slides height", getExtension());
        telemetry.addData("targetPos", targetPos);
        telemetry.addData("Slide State", slidesState);
        telemetry.addData("Error", slidesPID.error);
        telemetry.addData("Anti-Gravity", SLIDE_KGRAVITY * Math.sin(Math.toRadians(arm.getAngleDegrees())));
        telemetry.addData("PID Power", slidesPower);
        telemetry.addData("Proportion", slidesPID.P);
        telemetry.addData("Integral", slidesPID.I);
        telemetry.addData("Derivative", slidesPID.D);
    }
}
