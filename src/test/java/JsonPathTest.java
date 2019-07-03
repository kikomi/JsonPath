import com.kikomi.jsonpath.JsonPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class JsonPathTest {

    private final String testJson = "{\n" +
            "    \"clinics\": [\n" +
            "        {\n" +
            "            \"address1\": \"123 Steele St. S.\",\n" +
            "            \"phonenumber\": \"8139841921\",\n" +
            "            \"url\": \"\",\n" +
            "            \"zip\": \"83929\",\n" +
            "            \"city\": \"Tacoma\",\n" +
            "            \"name\": \"Tacoma South Medical Center\",\n" +
            "            \"state\": \"WA\",\n" +
            "            \"neighborhood\": \"Tacoma\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"address1\": \"123 W. North River Drive\",\n" +
            "            \"phonenumber\": \"4012938092\",\n" +
            "            \"zip\": \"85899\",\n" +
            "            \"city\": \"Spokane\",\n" +
            "            \"name\": \"Riverfront Medical Center\",\n" +
            "            \"state\": \"WA\",\n" +
            "            \"neighborhood\": \"Spokane\",\n" +
            "            \"innerClinic\": {\n" +
            "                \"address1\": \"123 N. Jefferson Lane\",\n" +
            "                \"phonenumber\": \"5096253700\",\n" +
            "                \"zip\": \"99201\",\n" +
            "                \"city\": \"Spokane\",\n" +
            "                \"name\": \"Kendall Yards Medical Office\",\n" +
            "                \"state\": \"WA\",\n" +
            "                \"neighborhood\": \"Kendall Yards Medical Office\",\n" +
            "                \"innerClinics\": [\n" +
            "                    {\n" +
            "                        \"address1\": \"123 Point Fosdick Drive\",\n" +
            "                        \"phonenumber\": \"8238929831\",\n" +
            "                        \"zip\": \"99929\",\n" +
            "                        \"city\": \"Gig Harbor\",\n" +
            "                        \"name\": \"Gig Harbor Medical Center\",\n" +
            "                        \"state\": \"WA\",\n" +
            "                        \"neighborhood\": \"Gig Harbor\",\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"address1\": \"123 39th Ave. S.E.\",\n" +
            "                        \"phonenumber\": \"1238889482\",\n" +
            "                        \"zip\": \"99991\",\n" +
            "                        \"city\": \"Puyallup\",\n" +
            "                        \"name\": \"Puyallup Medical Center\",\n" +
            "                        \"state\": \"WA\",\n" +
            "                        \"neighborhood\": \"Puyallup Medical Center\",\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"address1\": \"123 N. Nevada St.\",\n" +
            "                        \"phonenumber\": \"884398431\",\n" +
            "                        \"zip\": \"88123\",\n" +
            "                        \"city\": \"Spokane\",\n" +
            "                        \"name\": \"Northpointe Medical Office\",\n" +
            "                        \"state\": \"WA\",\n" +
            "                        \"neighborhood\": \"Northpointe Medical Office\",\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"address1\": \"123 Martin Luther King Jr. Way\",\n" +
            "            \"phonenumber\": \"9923923238\",\n" +
            "            \"zip\": \"92848\",\n" +
            "            \"city\": \"Tacoma\",\n" +
            "            \"name\": \"Tacoma Medical Center\",\n" +
            "            \"state\": \"WA\",\n" +
            "            \"neighborhood\": \"Tacoma\",\n" +
            "        },\n" +
            "        {\n" +
            "            \"address1\": \"123 N.E. 123th St.\",\n" +
            "            \"phonenumber\": \"993493491\",\n" +
            "            \"zip\": \"92992\",\n" +
            "            \"city\": \"Bothell\",\n" +
            "            \"name\": \"Northshore Medical Center\",\n" +
            "            \"state\": \"WA\",\n" +
            "            \"neighborhood\": \"Bothell\",\n" +
            "            \"innerClinic\": {\n" +
            "                \"address1\": \"123 154th Ave. W.\",\n" +
            "                \"phonenumber\": \"838928992\",\n" +
            "                \"zip\": \"99291\",\n" +
            "                \"city\": \"Lynnwood\",\n" +
            "                \"name\": \"Lynnwood Medical Center\",\n" +
            "                \"state\": \"WA\",\n" +
            "                \"neighborhood\": \"Lynnwood\",\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"address1\": \"123 S. 421th St.\",\n" +
            "            \"phonenumber\": \"839823982\",\n" +
            "            \"zip\": \"83829\",\n" +
            "            \"city\": \"Federal Way\",\n" +
            "            \"name\": \"Federal Way Medical Center\",\n" +
            "            \"state\": \"WA\",\n" +
            "            \"neighborhood\": \"Federal Way\",\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    private JsonPath jsonPath = new JsonPath(testJson);

    @Test
    public void parseSuccessTest() {
        JSONObject value = jsonPath.read("$.*[@.name='Puyallup Medical Center']", JSONObject.class);
        Assert.assertEquals("Puyallup", value.getString("city"));
        Assert.assertEquals("99991", value.getString("zip"));

        String city = jsonPath.read("$.*[@.name='Kendall Yards Medical Office'].innerClinics.*[@.name='Gig Harbor Medical Center'].city", String.class);
        Assert.assertEquals("Gig Harbor", city);
    }

    @Test
    public void parseSuccess2Test() {
        String parentAttrName1 = jsonPath.read("$.*[@.name='Riverfront Medical Center'].innerClinic.*[@.name='Northpointe Medical Office'].@parent", String.class);
        Assert.assertEquals("innerClinics", parentAttrName1);

        String parentAttrName2 = jsonPath.read("$.*[@.name='Kendall Yards Medical Office'].@parent", String.class);
        Assert.assertEquals("innerClinic", parentAttrName2);
    }

    @Test
    public void parseSuccess3Test() {
        JSONObject jsonObject = jsonPath.read("$.*[@.name='Riverfront Medical Center'].innerClinic.*[@.name='Puyallup Medical Center']", JSONObject.class);
        Assert.assertEquals("Puyallup Medical Center", jsonObject.getString("neighborhood"));
        Assert.assertEquals("1238889482", jsonObject.getString("phonenumber"));
        Assert.assertEquals("Puyallup", jsonObject.getString("city"));
    }

    @Test
    public void parseSuccess4Test() {
        JSONArray jsonArray = jsonPath.read("$.*[@.name='Kendall Yards Medical Office'].innerClinics", JSONArray.class);
        Assert.assertNotNull(jsonArray);
        Assert.assertEquals(3, jsonArray.length());
        Assert.assertEquals("Gig Harbor Medical Center", jsonArray.getJSONObject(0).getString("name"));
        Assert.assertEquals("Puyallup Medical Center", jsonArray.getJSONObject(1).getString("name"));
        Assert.assertEquals("Northpointe Medical Office", jsonArray.getJSONObject(2).getString("name"));
    }

    @Test
    public void parseSuccess5Test() {
        JSONObject jsonObject = jsonPath.read("$.*[@.name='Riverfront Medical Center'].innerClinic", JSONObject.class);
        Assert.assertEquals("Kendall Yards Medical Office", jsonObject.getString("neighborhood"));
        Assert.assertEquals("Kendall Yards Medical Office", jsonObject.getString("name"));
        Assert.assertEquals("Spokane", jsonObject.getString("city"));
    }

    @Test
    public void parseSuccess6Test() {
        JSONObject jsonObject = jsonPath.read("$.*[@.name]", JSONObject.class);
        Assert.assertEquals("Tacoma South Medical Center", jsonObject.getString("name"));
        Assert.assertEquals("Tacoma", jsonObject.getString("neighborhood"));
        Assert.assertEquals("Tacoma", jsonObject.getString("city"));
    }

    @Test
    public void parseSuccess7Test() {
        JSONArray jsonArray = jsonPath.read("$.*[@.name = 'Kendall Yards Medical Office'].innerClinics", JSONArray.class);
        Assert.assertEquals(3, jsonArray.length());
        Assert.assertEquals("Gig Harbor Medical Center", jsonArray.getJSONObject(0).getString("name"));
        Assert.assertEquals("Puyallup Medical Center", jsonArray.getJSONObject(1).getString("name"));
        Assert.assertEquals("Northpointe Medical Office", jsonArray.getJSONObject(2).getString("name"));

        jsonArray = jsonPath.read("$.*[@.name = \"Kendall Yards Medical Office\"].innerClinics", JSONArray.class);
        Assert.assertEquals(3, jsonArray.length());
        Assert.assertEquals("Gig Harbor Medical Center", jsonArray.getJSONObject(0).getString("name"));
        Assert.assertEquals("Puyallup Medical Center", jsonArray.getJSONObject(1).getString("name"));
        Assert.assertEquals("Northpointe Medical Office", jsonArray.getJSONObject(2).getString("name"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseFailureTest() {
        jsonPath.read(null, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseFailure2Test() {
        jsonPath.read("$*[@.name='Riverfront Medical Center']", String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseFailure3Test() {
        jsonPath.read("$.*[@.name='Riverfront Medical Center'].innerClinic.*[@.name='Northpointe Medical Office'.@parent", String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseFailure4Test() {
        jsonPath.read("$.*[].name", JSONObject.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseFailure5Test() {
        jsonPath.read("$.*.", JSONObject.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseFailure6Test() {
        jsonPath.read("$.*[@.name]", String.class);
    }
}