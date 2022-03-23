package hu.blackbelt.judo.tatami.jsl.workflow.maven.plugin;

public class DialectParam {

    private String rdbms;
    private String liquibase;
    private String asm2rdbmsTrace;

    public String getRdbms() {
        return rdbms;
    }

    public void setRdbms(String rdbms) {
        this.rdbms = rdbms;
    }

    public String getLiquibase() {
        return liquibase;
    }

    public void setLiquibase(String liquibase) {
        this.liquibase = liquibase;
    }

    public String getAsm2rdbmsTrace() {
        return asm2rdbmsTrace;
    }

    public void setAsm2rdbmsTrace(String asm2rdbmsTrace) {
        this.asm2rdbmsTrace = asm2rdbmsTrace;
    }
}
