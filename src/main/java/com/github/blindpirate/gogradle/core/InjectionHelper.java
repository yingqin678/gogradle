package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyProduceStrategy;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyFactory;
import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class InjectionHelper {

    @SuppressWarnings({"checkstyle:staticvariablename", "checkstyle:visibilitymodifier"})
    @SuppressFBWarnings("MS_CANNOT_BE_FINAL")
    public static Injector INJECTOR_INSTANCE;

    public static GolangDependencySet parseMany(Collection<? extends Map> notations, MapNotationParser parser) {
        GolangDependencySet ret = new GolangDependencySet();
        for (Map notation : notations) {
            ret.add(parser.parse(notation));
        }
        return ret;
    }

    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    public static Optional<GolangDependencySet> produceDependencies(GolangPackageModule module) {
        return INJECTOR_INSTANCE.getInstance(DependencyFactory.class).produce(module);
    }

    public static <T extends DependencyProduceStrategy> T strategy(Class<T> clazz) {
        return INJECTOR_INSTANCE.getInstance(clazz);
    }
}
