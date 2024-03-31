package org.firstinspires.ftc.teamcode.auto;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantFunction;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcontroller.Hardware.Intakes.ServoClaw;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.Mechanisms.TenacityArm;
import org.firstinspires.ftc.teamcode.Mechanisms.TenacityClaw;
import org.firstinspires.ftc.teamcode.Mechanisms.TenacitySlides;
import org.firstinspires.ftc.teamcode.Mechanisms.TenacityWrist;

@Autonomous
public class TenacityAuto extends LinearOpMode {
    TenacityArm arm;
    TenacitySlides slides;
    TenacityWrist wrist;
    TenacityClaw doubleClaw;

    ElapsedTime runtime = new ElapsedTime();
    @Override
    public void runOpMode() throws InterruptedException {
        arm = new TenacityArm(slides,gamepad1, telemetry, hardwareMap);
        slides = new TenacitySlides(arm, gamepad1, telemetry, hardwareMap);
        wrist = new TenacityWrist(gamepad1, telemetry, hardwareMap);
        doubleClaw = new TenacityClaw(gamepad1, telemetry, hardwareMap);

        arm.slides = slides;
        slides.arm = arm;

        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(12, 59, Math.toRadians(270)));

        InstantFunction prepareToPlacePurplePixel = () -> {
            arm.armState = TenacityArm.ArmState.FAR_INTAKE;
            slides.slidesState = TenacitySlides.SlidesState.FAR_INTAKE;
            wrist.wristPosition = TenacityWrist.WristPosition.INTAKE_POSITION;
        };

        InstantFunction placePurplePixel = () -> {
            doubleClaw.clawState = TenacityClaw.ClawState.LEFT_OPEN;
        };

        InstantFunction prepareToPlaceYellowPixel = () -> {
            arm.armState = TenacityArm.ArmState.FIRST_LINE;
            slides.slidesState = TenacitySlides.SlidesState.FIRST_LINE;
            wrist.wristPosition = TenacityWrist.WristPosition.OUTTAKE_FIRST_LINE;
        };

        InstantFunction placeYellowPixel = () -> {
            doubleClaw.clawState = TenacityClaw.ClawState.RIGHT_OPEN;
        };

        Action traj = drive.actionBuilder(new Pose2d(12, 59, Math.toRadians(270)))
                .strafeToLinearHeading(new Vector2d(42, 18), Math.toRadians(180))
                .stopAndAdd(prepareToPlacePurplePixel)
                .waitSeconds(2)
                .stopAndAdd(placePurplePixel)
                .waitSeconds(1)
                .stopAndAdd(prepareToPlaceYellowPixel)
                // purple pixel

                .strafeTo(new Vector2d(46, 34))
                // prepare to outtake yellow
                .waitSeconds(0.25)
                .stopAndAdd(placeYellowPixel)
                // outtake yellow
                // arm to drive mode
                .build();



        Action updateArm = (t) -> {
            slides.setSlidePower();
            arm.setArmPower();
            wrist.setWristPosition();
            doubleClaw.setClawPosition();
            return true;
        };

        runtime.reset();

        while (!isStarted()) {
            slides.setSlidePower();
            doubleClaw.setClawClosed();
            wrist.setWristPosition();
            doubleClaw.clawState = TenacityClaw.ClawState.CLOSED;

            if (runtime.seconds() > 3){
                arm.setArmPower();
            }

            telemetry.addLine("Waiting For Start");
            telemetry.update();
        }


        Actions.runBlocking(
                new ParallelAction(
                        traj,
                        updateArm
                )
        );
    }


}