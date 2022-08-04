package util;

import com.regnosys.rosetta.common.util.ClassPathUtils;
import com.regnosys.rosetta.rosetta.*;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.transgest.ModelLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * TODO: Util needs to determine if types are used or not
 */
public class UnusedModelElementFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnusedModelElementFinder.class);
    private static final Set<String> listOfTypes = new HashSet<>();
    private static final Set<String> listOfUsedTypes = new HashSet<>();
    private static final Set<String> listOfUnusedTypes = new HashSet<>();

    private static List<RosettaModel> models;

    public void run() throws IOException {

        ModelLoaderImpl modelLoader = new ModelLoaderImpl(ClassPathUtils.findRosettaFilePaths().stream().map(ClassPathUtils::toUrl).toArray(URL[]::new));

        models = modelLoader.models();
        generateTypesList();

        LOGGER.info("{} Types found in Model ", listOfTypes.size());
        LOGGER.info("{} Types are used within Model", listOfUsedTypes.size());
        listOfUnusedTypes.addAll(listOfTypes);
        listOfUnusedTypes.removeAll(listOfUsedTypes);

        LOGGER.info("out of which {} are now orphaned types as listed below:", listOfUnusedTypes.size());

        // LOGGER.info("orphaned Type: {}", orphanedTypes);
        Arrays.stream(listOfUnusedTypes.toArray()).sorted()
                .forEach(System.out::println);

    }

    private String getQualifiedName(RosettaType type) {
        return type.getModel().getName() + "." + type.getName();
    }

    private void generateTypesList() {

        for (RosettaModel model : models) {
            //LOGGER.info("Processing namespace {}, containing {} model elements", model.getName(), model.getElements().size());

            model.getElements().stream()
                    .filter(Data.class::isInstance)
                    .map(Data.class::cast)
                    .forEach(dataType -> {
                        listOfTypes.add(getQualifiedName(dataType));
                        dataType.getAttributes().stream()
                                .map(RosettaTyped::getType)
                                .filter(t -> !RosettaBuiltinType.class.isInstance(t))
                                .forEach(attributeType -> listOfUsedTypes.add(getQualifiedName(attributeType)));
                    });

            model.getElements().stream()
                    .filter(RosettaEnumeration.class::isInstance)
                    .map(RosettaEnumeration.class::cast)
                    .forEach(enumeration -> {
                        // LOGGER.info("{}", getQualifiedName(enumeration));
                        listOfTypes.add(getQualifiedName(enumeration));
                    });

            model.getElements().stream()
                    .filter(Function.class::isInstance)
                    .map(Function.class::cast)
                    .forEach(function -> {
                        // LOGGER.info("{}.{}", function.getModel().getName(), function.getName());
                        function.getInputs()
                                .forEach(inputs -> {
                                    // LOGGER.info("  Processing input Types used within function {}", inputs.getName());
                                    listOfUsedTypes.add(inputs.getType().getName());
                                });
                        if (function.getOutput() != null) {
                            //LOGGER.info("Processing output Types used within function {}", function.getOutput().getName());
                            listOfUsedTypes.add(function.getOutput().getType().getName());
                        }
                        listOfTypes.add(function.getModel().getName().concat(function.getName()));
                    });
        }

    }

    public static void main(String[] args) throws IOException {
        new UnusedModelElementFinder().run();
    }
}