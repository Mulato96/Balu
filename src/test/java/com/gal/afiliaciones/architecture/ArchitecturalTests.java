package com.gal.afiliaciones.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * Tests arquitectónicos para validar buenas prácticas y patrones de diseño
 * en el proyecto de afiliaciones.
 * 
 * @author GAL
 * @version 1.0
 */
@AnalyzeClasses(packages = "com.gal.afiliaciones", 
    importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitecturalTests {

    /**
     * Validación para detectar el uso inadecuado de Repository.findAll() 
     * seguido de operaciones stream. Se recomienda usar @Query para
     * optimizar las consultas a la base de datos.
     * 
     * Esta regla viola el principio de rendimiento al cargar todos los
     * registros de una tabla y luego filtrarlos en memoria, lo cual
     * puede causar problemas de memoria y lentitud con datasets grandes.
     */
    @ArchTest
    static final ArchRule noRepositoryFindAllStream = methods()
        .that().areDeclaredInClassesThat()
            .resideInAnyPackage("..service..", "..controller..", "..component..")
        .should(new ArchCondition<JavaMethod>("not use Repository.findAll() with stream operations") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                method.getMethodCallsFromSelf().forEach(call -> {
                    // Verificar que sea un método findAll
                    if (call.getTarget().getName().equals("findAll")) {
                        JavaClass ownerClass = call.getTarget().getOwner();
                        
                        // Verificar que el tipo sea Repository de Spring
                        boolean isSpringRepository = ownerClass.isAssignableTo("org.springframework.data.repository.Repository");
                        
                        // O verificar que esté en paquete de repositorios
                        boolean isInRepositoryPackage = ownerClass.getPackageName().contains(".repository");
                        
                        if (isSpringRepository || isInRepositoryPackage) {
                            boolean hasParameters = !call.getTarget().getRawParameterTypes().isEmpty();
                
                            if (hasParameters) {
                                // findAll(Specification) o findAll(Pageable) ya están optimizados
                                return; // No validar este caso
                            }
                            // Buscar si hay stream/filter después
                            boolean hasStreamOperation = method.getMethodCallsFromSelf().stream()
                                .anyMatch(c -> 
                                    (c.getTarget().getName().equals("filter") ||
                                     c.getTarget().getName().equals("sorted") ||
                                     c.getTarget().getName().equals("limit") ||
                                     c.getTarget().getName().equals("skip") ||
                                     c.getTarget().getName().equals("distinct") ||
                                     c.getTarget().getName().equals("findFirst") ||
                                     c.getTarget().getName().equals("anyMatch"))
                                );
                            
                            if (hasStreamOperation) {
                                String message = String.format(
                                    "%s:%d llama findAll() en %s seguido de operaciones stream. Usa metodos personalizados en su lugar.",
                                    method.getSourceCodeLocation().getSourceClass().getSimpleName(),
                                    call.getLineNumber(),
                                    ownerClass.getSimpleName()
                                );
                                events.add(SimpleConditionEvent.violated(method, message));
                            }
                        }
                    }
                });
            }
        });

}