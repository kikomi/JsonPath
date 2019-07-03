package com.kikomi.jsonpath;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonPath definition.
 * This is a custom implementation of a JsonPath processor similar to
 * <code>"com.jayway.jsonpath.JsonPath"</code> and analogs. The reason behind implementing this custom JsonPath processor
 * is in the fact that none of the existing JSONPath implementations allow to retrieve a property of a parent object that
 * JSONPath-matched object is assigned to.
 * <p>
 * Example:
 * Assume, we need to find a clinic object having only its name and extract the ClinicID for that object where
 * JSON structure looks like below.
 * Here ClinicID is represented by a property named "clinics_123".
 * <pre>
 *     {
 *     "clinics": {
 *         "clinics_123": {
 *             "address": "123 Washington Way",
 *             "accessibility": "true",
 *             "zip": "99011",
 *             "city": "Seattle",
 *             "name": "Seattle Medical Center",
 *             "state": "WA"
 *         }
 *     },...
 * }
 * </pre>
 * In order to retrieve this property JsonPath has to look like <code>"$.*[@.name='Seattle Medical Center'].@parent"<code/>
 * where <code>"@parent"<code/> is a chunk denoting that parent object's property, which found object belongs to, has to be returned.
 * The result of the JSONPath query from above will be "clinics_123".
 * This functionality is not currently supported by any existing JsonPath libraries. However much limited comparing to
 * <code>"com.jayway.jsonpath.JsonPath"</code> this implementation can be extended to accommodate additional functionality.
 */
public class JsonPath {

    private static final String PATTERN_PREFIX = "$.";
    private static final String ATTRIBUTE_CONDITION_PREFIX = "@.";
    private static final String PATTERN_PARENT_ATTRIBUTE = "@parent";
    private static final char CONDITION_PREFIX = '[';
    private static final char CONDITION_SUFFIX = ']';
    private static final char CONDITION_DOUBLE_QUOTES_ENCLOSURE = '"';
    private static final char CONDITION_SINGLE_QUOTES_ENCLOSURE = '\'';
    private static final char CONDITION_EQUALS_COMPARISON = '=';

    private final JSONObject jsonObject;

    /**
     * Initializes a {@link JsonPath} from {@link String}
     *
     * @param json JSON-string
     */
    public JsonPath(final String json) {
        this.jsonObject = new JSONObject(json);
    }

    /**
     * Initializes a {@link JsonPath} from {@link JSONObject}
     *
     * @param jsonObject JSON-object
     */
    public JsonPath(final JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * Returns a first object that matches specified JPath.
     *
     * @param jPath          JPaths to match
     * @param classReference return type
     * @param <T>            return type
     * @return a first object that matches @param paths
     */
    public <T> T read(final String jPath, final Class<T> classReference) {
        if (jPath == null || jPath.length() == 0)
            throw new IllegalArgumentException("JPath should be specified.");

        if (!jPath.startsWith(PATTERN_PREFIX))
            throw new IllegalArgumentException(String.format("JPath should always start with \"%s\"", PATTERN_PREFIX));

        final LinkedList<Path> paths = getPaths(jPath);

        Object match = matchFirst(paths, this.jsonObject);

        if (match == null)
            return null;

        if (!classReference.isInstance(match))
            throw new IllegalArgumentException("The object found doesn't represent the type specified in parameter.");

        return (T) match;
    }

    /**
     * Finds a first matching object according to JPath.
     *
     * @param paths JPaths to match
     * @param json  object to search in
     * @param <T>   return type
     * @return a first object that matches @param paths
     */
    private <T> T matchFirst(final LinkedList<Path> paths, final Object json) {
        Path currentPath = paths.pop();
        List<SearchObject> matching = findMatching(currentPath, json);

        while (!paths.isEmpty()) {
            currentPath = paths.pop();

            // getting only the parent attribute that current object belongs to
            if (currentPath.path.equals(PATTERN_PARENT_ATTRIBUTE))
                return matching.size() > 0 ? (T) matching.get(0).propertyName : null;

            List<SearchObject> found = new ArrayList<>();
            for (final SearchObject searchObject : matching) {
                found.addAll(findMatching(currentPath, searchObject.object));
            }
            matching = found;
        }
        return matching.size() > 0 ? (T) matching.get(0).object : null;
    }

    /**
     * Finds object by {@link Path}.
     *
     * @param path {@link Path} to use to find object
     * @param json current object to search in
     * @return a list of found objects
     */
    private List<SearchObject> findMatching(final Path path, final Object json) {
        final List<SearchObject> result = new ArrayList<>();
        if (path.path.equals("*")) {
            final LinkedList<SearchObject> queue = new LinkedList<>();
            queue.push(new SearchObject("", json));
            do {
                SearchObject searchObject = queue.pop();
                Object current = searchObject.object;
                if (current instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) current;
                    if (path.condition != null) {
                        if (path.condition.matches(jsonObject)) {
                            result.add(searchObject);
                            break;
                        }
                    } else {
                        result.add(searchObject);
                    }

                    for (final String key : jsonObject.keySet()) {
                        Object object = jsonObject.opt(key);
                        if (object instanceof JSONObject || object instanceof JSONArray)
                            queue.add(new SearchObject(key, object));
                    }
                } else {
                    if (current instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) current;
                        jsonArray.forEach(c -> queue.add(new SearchObject(searchObject.propertyName, c)));
                    }
                }
            } while (!queue.isEmpty());
        } else {
            if (json instanceof JSONObject) {
                Object object = ((JSONObject) json).opt(path.path);
                if (object != null)
                    result.add(new SearchObject(path.path, object));
            }
        }

        return result;
    }

    /**
     * Get paths from JPath string.
     *
     * @param jPath a string representing JPath
     * @return JPaths as {@link LinkedList<Path>}
     */
    private LinkedList<Path> getPaths(final String jPath) {
        final LinkedList<Path> result = new LinkedList<>();
        // skipping "$."
        for (int i = 2; i < jPath.length(); i++) {
            StringBuilder pathString = new StringBuilder();
            char current = jPath.charAt(i);
            while (current != '.' && current != CONDITION_PREFIX) {
                pathString.append(current);
                if (++i < jPath.length())
                    current = jPath.charAt(i);
                else break;
            }

            if (i == jPath.length() - 1 && !Character.isAlphabetic(current))
                throw new IllegalArgumentException("JPath path chunk can not end with special characters.");

            Condition condition = null;
            if (current == CONDITION_PREFIX) {
                if (++i < jPath.length()) {
                    StringBuilder conditionString = new StringBuilder();
                    current = jPath.charAt(i);
                    while (current != CONDITION_SUFFIX) {
                        conditionString.append(current);
                        if (++i < jPath.length())
                            current = jPath.charAt(i);
                        else break;
                    }

                    if (current != CONDITION_SUFFIX)
                        throw new IllegalArgumentException(String.format("JPath condition chunk should end with \"%s\".", CONDITION_SUFFIX));

                    if (conditionString.length() == 0)
                        throw new IllegalArgumentException("JPath condition chunk can not be empty.");

                    condition = parseCondition(conditionString.toString());

                    // going to the next index after ']'
                    i++;
                } else
                    throw new IllegalArgumentException(String.format("JPath condition chunk should start with \"%s\" and end with \"%s\".", CONDITION_PREFIX, CONDITION_SUFFIX));
            }

            Path path = new Path(pathString.toString(), condition);
            result.add(path);
        }
        return result;
    }

    /**
     * Parses a condition.
     *
     * @param condition a string representing condition to parse {@link Condition} from.
     * @return {@link Condition} object.
     */
    private Condition parseCondition(final String condition) {
        if (condition.startsWith(ATTRIBUTE_CONDITION_PREFIX)) {
            return new AttrCondition(condition);
        }
        // TODO: this parsed can be extended if needed
        return null;
    }

    /**
     * Json Path definition.
     */
    private class Path {
        private final String path;
        private final Condition condition;

        /**
         * Initializes a {@link Path} object.
         *
         * @param path      JPath chunk
         * @param condition condition associated with current JPath chunk
         */
        private Path(final String path,
                     final Condition condition) {
            this.path = path;
            this.condition = condition;
        }
    }

    /**
     * Matching condition definition.
     */
    private interface Condition {
        /**
         * Checks condition on the {@link JSONObject} specified.
         *
         * @param object {@link JSONObject} to check
         * @return {@literal true} if condition matches, otherwise {@literal false}
         */
        boolean matches(final JSONObject object);
    }

    /**
     * Attribute matching condition.
     */
    private class AttrCondition implements Condition {
        String attributeName;

        Pattern attributeValueRegExp = null;

        private AttrCondition(final String conditionDefinition) {
            this.parse(conditionDefinition);
        }

        /**
         * Parses out the attribute condition from the {@link String}
         *
         * @param conditionDefinition {@link String} representation of a condition
         */
        private void parse(final String conditionDefinition) {
            if (!conditionDefinition.startsWith(ATTRIBUTE_CONDITION_PREFIX))
                throw new IllegalArgumentException(String.format("Condition has to start with \"%s\".", ATTRIBUTE_CONDITION_PREFIX));

            // skipping first "@."
            for (int i = 2; i < conditionDefinition.length(); i++) {
                char current = conditionDefinition.charAt(i);
                StringBuilder attrName = new StringBuilder();

                boolean isAttributeComplete = false;
                boolean hasEqualsSign = false;
                while (current != CONDITION_DOUBLE_QUOTES_ENCLOSURE && current != CONDITION_SINGLE_QUOTES_ENCLOSURE) {
                    // filling up the attribute name until equals or space sign
                    if (!isAttributeComplete && current != ' ' && current != CONDITION_EQUALS_COMPARISON) {
                        attrName.append(current);
                    } else {
                        isAttributeComplete = true;
                        if (current == CONDITION_EQUALS_COMPARISON)
                            hasEqualsSign = true;
                    }

                    if (++i < conditionDefinition.length())
                        current = conditionDefinition.charAt(i);
                    else break;
                }

                if (hasEqualsSign && i == conditionDefinition.length()) {
                    throw new IllegalArgumentException(String.format("Condition \"%s\" doesn't specify a value.", conditionDefinition));
                }

                i++;
                if (i < conditionDefinition.length()) {
                    StringBuilder attValue = new StringBuilder(conditionDefinition.length() - i);
                    current = conditionDefinition.charAt(i);
                    while (current != CONDITION_SINGLE_QUOTES_ENCLOSURE && current != CONDITION_DOUBLE_QUOTES_ENCLOSURE) {
                        attValue.append(current);

                        if (++i < conditionDefinition.length())
                            current = conditionDefinition.charAt(i);
                        else break;
                    }
                    this.attributeValueRegExp = Pattern.compile(attValue.toString());
                }
                this.attributeName = attrName.toString();
            }
        }

        /**
         * Checks condition on the {@link JSONObject} specified.
         *
         * @param object {@link JSONObject} to check
         * @return {@literal true} if condition matches, otherwise {@literal false}
         */
        @Override
        public boolean matches(final JSONObject object) {
            if (object == null)
                return false;

            if (object.has(this.attributeName)) {
                final Object attrValue = object.get(this.attributeName);
                if (attrValue == null) {
                    return this.attributeValueRegExp == null;
                } else {
                    if (this.attributeValueRegExp != null) {
                        final String valueString = attrValue.toString();
                        final Matcher matcher = attributeValueRegExp.matcher(valueString);
                        return matcher.matches();
                    } else
                        // we want objects that at least have this attribute if filter is [@.attrName]
                        return true;
                }
            }

            return false;
        }
    }

    /**
     * Search object.
     * Represents a search result.
     */
    private class SearchObject {
        private final String propertyName;
        private final Object object;

        /**
         * Initializes a {@link SearchObject}
         *
         * @param propertyName JSON property name of the parent object that current object is associated with
         * @param object       the object found
         */
        private SearchObject(final String propertyName, final Object object) {
            this.propertyName = propertyName;
            this.object = object;
        }
    }
}
