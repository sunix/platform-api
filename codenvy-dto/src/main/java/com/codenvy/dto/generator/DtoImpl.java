// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.codenvy.dto.generator;

import com.codenvy.dto.shared.CompactJsonDto;
import com.codenvy.dto.shared.DelegateTo;
import com.codenvy.dto.shared.SerializationIndex;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Abstract base class for the source generating template for a single DTO. */
abstract class DtoImpl {
    private final Class<?>     dtoInterface;
    private final DtoTemplate  enclosingTemplate;
    private final boolean      compactJson;
    private final String       implClassName;
    private final List<Method> dtoMethods;

    DtoImpl(DtoTemplate enclosingTemplate, Class<?> dtoInterface) {
        this.enclosingTemplate = enclosingTemplate;
        this.dtoInterface = dtoInterface;
        this.implClassName = dtoInterface.getSimpleName() + "Impl";
        this.compactJson = DtoTemplate.implementsInterface(dtoInterface, CompactJsonDto.class);
        this.dtoMethods = ImmutableList.copyOf(calcDtoMethods());
    }

    protected boolean isCompactJson() {
        return compactJson;
    }

    public Class<?> getDtoInterface() {
        return dtoInterface;
    }

    public DtoTemplate getEnclosingTemplate() {
        return enclosingTemplate;
    }

    protected String getFieldName(String methodName) {
        String fieldName;
        if (methodName.startsWith("get")) {
            fieldName = methodName.substring(3);
        } else {
            // starts with "is", see method '#ignoreMethod(Method)'
            fieldName = methodName.substring(2);
        }
        fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
        return fieldName;
    }

    protected String getImplClassName() {
        return implClassName;
    }

    protected String getSetterName(String fieldName) {
        return "set" + getCamelCaseName(fieldName);
    }

    protected String getWithName(String fieldName) {
        return "with" + getCamelCaseName(fieldName);
    }

    protected String getListAdderName(String fieldName) {
        return "add" + getCamelCaseName(fieldName);
    }

    protected String getMapPutterName(String fieldName) {
        return "put" + getCamelCaseName(fieldName);
    }

    protected String getClearName(String fieldName) {
        return "clear" + getCamelCaseName(fieldName);
    }

    protected String getEnsureName(String fieldName) {
        return "ensure" + getCamelCaseName(fieldName);
    }

    protected String getCamelCaseName(String fieldName) {
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * Our super interface may implement some other interface (or not). We need to know because if it does then we need to directly extend
     * said super interfaces impl class.
     */
    protected Class<?> getSuperInterface() {
        Class<?>[] superInterfaces = dtoInterface.getInterfaces();
        return superInterfaces.length == 0 ? null : superInterfaces[0];
    }

    /**
     * We need not generate a field and method for any method present on a parent interface that our interface may inherit from. We only
     * care about the new methods defined on our superInterface.
     */
    protected boolean ignoreMethod(Method method) {
        if (method == null) {
            return true;
        }
        if (!isDtoGetter(method)) {
            return true;
        }
        if (method.isAnnotationPresent(DelegateTo.class)) {
            return true;
        }
        // Look at any interfaces our superInterface implements.
        Class<?>[] superInterfaces = dtoInterface.getInterfaces();
        List<Method> methodsToExclude = new LinkedList<>();
        // Collect methods on parent interfaces
        for (Class<?> parent : superInterfaces) {
            Collections.addAll(methodsToExclude, parent.getMethods());
        }
        for (Method m : methodsToExclude) {
            if (m.equals(method)) {
                return true;
            }
        }
        return false;
    }

    /** Check is specified method is DTO getter. */
    protected boolean isDtoGetter(Method method) {
        if (method.isAnnotationPresent(DelegateTo.class)) {
            return false;
        }
        String methodName = method.getName();
        if ((methodName.startsWith("get") || methodName.startsWith("is")) && method.getParameterTypes().length == 0) {
            if (methodName.startsWith("is") && methodName.length() > 2) {
                return method.getReturnType() == Boolean.class || method.getReturnType() == boolean.class;
            }
            return methodName.length() > 3;
        }
        return false;
    }

    /** Tests whether or not a given generic type is allowed to be used as a generic. */
    protected static boolean isWhitelisted(Class<?> genericType) {
        return DtoTemplate.jreWhitelist.contains(genericType);
    }

    /** Tests whether or not a given return type is a number primitive or its wrapper type. */
    protected static boolean isNumber(Class<?> returnType) {
        final Class<?>[] numericTypes = {int.class, long.class, short.class, float.class, double.class, byte.class,
                                         Integer.class, Long.class, Short.class, Float.class, Double.class, Byte.class};
        for (Class<?> standardPrimitive : numericTypes) {
            if (returnType.equals(standardPrimitive)) {
                return true;
            }
        }
        return false;
    }

    /** Tests whether or not a given return type is a boolean primitive or its wrapper type. */
    protected static boolean isBoolean(Class<?> returnType) {
        return returnType.equals(Boolean.class) || returnType.equals(boolean.class);
    }

    protected static String getPrimitiveName(Class<?> returnType) {
        if (returnType.equals(Integer.class) || returnType.equals(int.class)) {
            return "int";
        } else if (returnType.equals(Long.class) || returnType.equals(long.class)) {
            return "long";
        } else if (returnType.equals(Short.class) || returnType.equals(short.class)) {
            return "short";
        } else if (returnType.equals(Float.class) || returnType.equals(float.class)) {
            return "float";
        } else if (returnType.equals(Double.class) || returnType.equals(double.class)) {
            return "double";
        } else if (returnType.equals(Byte.class) || returnType.equals(byte.class)) {
            return "byte";
        } else if (returnType.equals(Boolean.class) || returnType.equals(boolean.class)) {
            return "boolean";
        } else if (returnType.equals(Character.class) || returnType.equals(char.class)) {
            return "char";
        }
        throw new IllegalArgumentException("Unknown wrapper class type.");
    }

    /** Tests whether or not a given return type is a java.util.List. */
    public static boolean isList(Class<?> returnType) {
        return returnType.equals(List.class);
    }

    /** Tests whether or not a given return type is a java.util.Map. */
    public static boolean isMap(Class<?> returnType) {
        return returnType.equals(Map.class);
    }

    /**
     * Expands the type and its first generic parameter (which can also have a first generic parameter (...)).
     * <p/>
     * For example, JsonArray&lt;JsonStringMap&lt;JsonArray&lt;SomeDto&gt;&gt;&gt; would produce [JsonArray, JsonStringMap, JsonArray,
     * SomeDto].
     */
    public static List<Type> expandType(Type curType) {
        List<Type> types = new LinkedList<>();
        do {
            types.add(curType);

            if (curType instanceof ParameterizedType) {
                Type[] genericParamTypes = ((ParameterizedType)curType).getActualTypeArguments();
                Type rawType = ((ParameterizedType)curType).getRawType();
                boolean map = rawType instanceof Class<?> && rawType == Map.class;
                if (!map && genericParamTypes.length != 1) {
                    throw new IllegalStateException("Multiple type parameters are not supported (neither are zero type parameters)");
                }
                Type genericParamType = map ? genericParamTypes[1] : genericParamTypes[0];
                if (genericParamType instanceof Class<?>) {
                    Class<?> genericParamTypeClass = (Class<?>)genericParamType;
                    if (isWhitelisted(genericParamTypeClass)) {
                        assert genericParamTypeClass.equals(
                                String.class) : "For JSON serialization there can be only strings or DTO types. ";
                    }
                }
                curType = genericParamType;
            } else {
                if (curType instanceof Class) {
                    Class<?> clazz = (Class<?>)curType;
                    if (isList(clazz) || isMap(clazz)) {
                        throw new DtoTemplate.MalformedDtoInterfaceException(
                                "JsonArray and JsonStringMap MUST have a generic type specified (and no... ? " + "doesn't cut it!).");
                    }
                }
                curType = null;
            }
        } while (curType != null);
        return types;
    }

    public static Class<?> getRawClass(Type type) {
        return (Class<?>)((type instanceof ParameterizedType) ? ((ParameterizedType)type).getRawType() : type);
    }

    /**
     * Returns public methods specified in DTO interface.
     * <p/>
     * <p>For compact DTO (see {@link CompactJsonDto}) methods are ordered corresponding to {@link SerializationIndex} annotation.
     * <p/>
     * <p>Gaps in index sequence are filled with {@code null}s.
     */
    protected List<Method> getDtoMethods() {
        return dtoMethods;
    }

    private Method[] calcDtoMethods() {
        if (!compactJson) {
            return dtoInterface.getMethods();
        }

        Map<Integer, Method> methodsMap = new HashMap<>();
        int maxIndex = 0;
        for (Method method : dtoInterface.getMethods()) {
            SerializationIndex serializationIndex = method.getAnnotation(SerializationIndex.class);
            Preconditions.checkNotNull(serializationIndex, "Serialization index is not specified for %s in %s",
                                       method.getName(), dtoInterface.getSimpleName());

            // "53" is the number of bits in JS integer.
            // This restriction will allow to add simple bit-field
            // "serialization-skipping-list" in the future.
            int index = serializationIndex.value();
            Preconditions.checkState(index > 0 && index <= 53, "Serialization index out of range [1..53] for %s in %s",
                                     method.getName(), dtoInterface.getSimpleName());

            Preconditions.checkState(!methodsMap.containsKey(index), "Duplicate serialization index for %s in %s",
                                     method.getName(), dtoInterface.getSimpleName());

            maxIndex = Math.max(index, maxIndex);
            methodsMap.put(index, method);
        }

        Method[] result = new Method[maxIndex];
        for (int index = 0; index < maxIndex; index++) {
            result[index] = methodsMap.get(index + 1);
        }

        return result;
    }

    protected boolean isLastMethod(Method method) {
        Preconditions.checkNotNull(method);
        return method == dtoMethods.get(dtoMethods.size() - 1);
    }

    /**
     * @return String representing the source definition for the DTO impl as an inner class.
     */
    abstract String serialize();
}