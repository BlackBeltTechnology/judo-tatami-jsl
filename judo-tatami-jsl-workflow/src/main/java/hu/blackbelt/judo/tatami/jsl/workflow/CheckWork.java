package hu.blackbelt.judo.tatami.jsl.workflow;

import hu.blackbelt.judo.tatami.core.workflow.work.*;

import java.util.function.Supplier;

public class CheckWork implements Work {

    final Supplier<Boolean> check;

    CheckWork(Supplier<Boolean> check) {
        this.check = check;
    }

    @Override
    public String getName() {
        return "Check TransformationContext";
    }

    @Override
    public WorkReport call() {
        try {
            if (check.get()) {
                return new DefaultWorkReport(WorkStatus.COMPLETED);
            } else {
                return new DefaultWorkReport(WorkStatus.FAILED);
            }
        } catch (Exception e) {
            return new DefaultWorkReport(WorkStatus.FAILED, e);
        }
    }
}
