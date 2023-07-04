package nl.ing.lovebird.clienttokens.annotations;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes({"nl.ing.lovebird.clientokens.annotations.AIS", "nl.ing.lovebird.clientokens.annotations.PIS"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ClientTokenClaimProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<Element> annotated = new HashSet<>(roundEnv.getElementsAnnotatedWith(AIS.class));
        annotated.addAll(roundEnv.getElementsAnnotatedWith(PIS.class));
        annotated.addAll(roundEnv.getElementsAnnotatedWith(NonDeletedClient.class));
        for (Element element : annotated) {
            boolean missingVerifiedClientToken = true;
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                for (VariableElement parameter : method.getParameters()) {
                    if (parameter.getAnnotation(VerifiedClientToken.class) != null) {
                        missingVerifiedClientToken = false;
                    }
                }
            }
            if (missingVerifiedClientToken) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("@VerifiedClientToken not found in %s.", element.getSimpleName()),
                        element);
            }
        }
        return true;
    }
}
