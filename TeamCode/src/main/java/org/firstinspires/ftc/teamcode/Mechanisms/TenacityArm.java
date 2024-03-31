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
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

@Config
public class TenacityArm extends MotorArm {
    Gamepad gamepad1;
    Telemetry telemetry;
    static String[] names = {"leftArm", "rightArm"};

    double ArmPower = 0;
    double targetAngle;


    public enum ArmState {
        INIT,
        CLOSE_INTAKE,
        FAR_INTAKE,
        DRIVING,
        FIRST_LINE,
        SECOND_LINE,
        THIRD_LINE,
        HANG,
        MANUAL
    }

    public PID_Controller ArmPID;

    public ArmState armState;


    public static PIDCoefficients ARM_PID_COEFFICIENTS = new PIDCoefficients(0.04, 0.0025, 0.003);
    public static double CLOSE_INTAKE_ANGLE = -12;
    public static double FAR_INTAKE_ANGLE = -11;
    public static double INIT_ANGLE = 30;
    public static double K_GRAVITY = 0.022;
    public static double DRIVING_ANGLE = -5;

    public Slides slides;
    public TenacityArm(Slides slides, Gamepad gamepad1, Telemetry telemetry, HardwareMap hardwareMap) {
        super(2, names, 28, 0.0091, 0.1, 45, hardwareMap);

        ArmPID = new PID_Controller(ARM_PID_COEFFICIENTS, 0);

        ArmPID.tolerance = 0.75;

        targetAngle = 45;

        armState = ArmState.INIT;

        this.gamepad1 = gamepad1;
        this.telemetry = telemetry;

        this.slides = slides;
    }

    public void setArmPower(){
        ArmPower = ArmPID.PID_Power(getAngleDegrees(), targetAngle);
        switch (armState){
            case INIT:
                targetAngle = INIT_ANGLE;
                break;
            case CLOSE_INTAKE:
                targetAngle = CLOSE_INTAKE_ANGLE;
                break;
            case DRIVING:
                targetAngle = DRIVING_ANGLE;
                break;
            case FAR_INTAKE:
                targetAngle = FAR_INTAKE_ANGLE;
                break;
            case FIRST_LINE:
                targetAngle = 152;
                break;

            case SECOND_LINE:
                targetAngle = 133;
                break;

            case THIRD_LINE:
                targetAngle = 130;
                break;
            case HANG:
                targetAngle = 105;
                break;
            case MANUAL:
                if (gamepad1.dpad_up){
                    ArmPower = -0.25;
                } else if (gamepad1.dpad_down){
                    ArmPower = 0.25;
                } else {
                    ArmPower = 0;
                }
                break;
        }
        setPower(ArmPower);
    }

    @Override
    public double getAngleDegrees() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        if (Math.abs(angles.thirdAngle) > 90 && angles.thirdAngle < 0){
            return 360 + angles.thirdAngle;
        } else{
            return angles.thirdAngle;
        }
    }

    public void setPower(double power){
        //TODO: check if gravity is exactly directly proportional with Cos(angle)
        antiGravity = K_GRAVITY*Math.cos(Math.toRadians(getAngleDegrees())); //*(slides.getExtension()+8);

        double totalPower = antiGravity + power;
        for (Motor motor : motors){
            motor.setPower(-totalPower);
        }
    }

    public void Telemetry(){
        telemetry.addData("ArmPos Degrees", getAngleDegrees());
        telemetry.addData("CookieMonster_Arm error", ArmPID.error);
        telemetry.addData("CookieMonster_Arm target angle", targetAngle);
        telemetry.addData("ArmState", armState);
        telemetry.addData("ArmPower", ArmPower);
    }

    public void TuningTelemetry(){
        telemetry.addData("Real Arm Power", antiGravity+ArmPower);
        telemetry.addData("ArmPos Degrees", getAngleDegrees());
        telemetry.addData("Arm error", ArmPID.error);
        telemetry.addData("target angle", targetAngle);
        telemetry.addData("ArmState", armState);
        telemetry.addData("ArmPower", ArmPower);
        telemetry.addData("Anti-Gravity", antiGravity);
        telemetry.addData("Proportion", ArmPID.P);
        telemetry.addData("Integral", ArmPID.I);
        telemetry.addData("Derivative", ArmPID.D);
    }
}
