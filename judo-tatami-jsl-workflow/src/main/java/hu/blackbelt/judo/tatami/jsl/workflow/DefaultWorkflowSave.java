package hu.blackbelt.judo.tatami.jsl.workflow;

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace;

import java.io.*;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.SaveArguments.psmSaveArgumentsBuilder;
import static hu.blackbelt.judo.tatami.jsl.workflow.ThrowingCosumerWrapper.executeWrapper;

public class DefaultWorkflowSave {

	private static final boolean VALIDATE_MODELS_ON_SAVE = false; // do not validate models on save

	public static void saveModels(TransformationContext transformationContext, File dest) {
		saveModels(true, transformationContext, dest);
	}

	public static void saveModels(boolean catchError, TransformationContext transformationContext, File dest) {

		if (!dest.exists()) {
			throw new IllegalArgumentException("Destination doesn't exist!");
		}
		if (!dest.isDirectory()) {
			throw new IllegalArgumentException("Destination is not a directory!");
		}

		transformationContext.getByClass(JslDslModel.class).ifPresent(executeWrapper(catchError, (m) ->
				m.saveJslDslModel(jslDslSaveArgumentsBuilder()
						.validateModel(VALIDATE_MODELS_ON_SAVE)
						.file(deleteFileIfExists(new File(dest, transformationContext.getModelName() + "-jsl.model"))))));

		transformationContext.getByClass(PsmModel.class).ifPresent(executeWrapper(catchError, (m) ->
				m.savePsmModel(psmSaveArgumentsBuilder()
						.validateModel(VALIDATE_MODELS_ON_SAVE)
						.file(deleteFileIfExists(new File(dest, transformationContext.getModelName() + "-psm.model"))))));

		transformationContext.getByClass(Jsl2PsmTransformationTrace.class).ifPresent(executeWrapper(catchError, (m) ->
				m.save(deleteFileIfExists(new File(dest, transformationContext.getModelName() + "-" + "jsl2psm.model")))));
	}

	private static File deleteFileIfExists(File file) {
		if (!file.isDirectory() && file.exists()) {
			file.delete();
		}
		return file;
	}
}
