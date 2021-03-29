package okhttp3.sample;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SampleServer extends Dispatcher {
    private final int port;

    public SampleServer(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        MockWebServer server = new MockWebServer();
        server.setDispatcher(this);
        server.start(port);
    }

    @Override
    public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath();
        if ("/slow-json".equals(path)) {
            final int responseThrottleTimeIn100Millis = 20;
            String shortJson = shortJson();
            final int bytesPerPeriod = shortJson.length() / responseThrottleTimeIn100Millis;
            return jsonResponseTemplate(shortJson)
                    .throttleBody(bytesPerPeriod, 100, TimeUnit.MILLISECONDS);
        } else if ("/long-json".equals(path)) {
            String jsonAsString = longJsonResponse();
            return jsonResponseTemplate(jsonAsString)
                    .throttleBody(jsonAsString.length(), 69, TimeUnit.MILLISECONDS);
        }
        return new MockResponse()
                .setStatus("HTTP/1.1 404")
                .addHeader("content-type: text/plain; charset=utf-8")
                .setBody("use /slow-json or /long-json for more fun")
                ;
    }

    @NotNull
    private MockResponse jsonResponseTemplate(String jsonAsString) {
        return new MockResponse()
                .setStatus("HTTP/1.1 200")
                .addHeader("content-type: application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(jsonAsString);
    }

    public static void main(String[] args) throws Exception {
        int port = 9090;
        SampleServer server = new SampleServer(port);
        server.run();
        System.out.println("server started on port " + port);
    }

    private String shortJson() {
        return "[{\"id\":4,\"someText\":\"883e11ad-0291-44cb-908b-6e18fd9b3888\"},{\"id\":5,\"someText\":\"" + UUID.randomUUID().toString() + "\"},{\"id\":6,\"someText\":\"" + UUID.randomUUID().toString() + "\"},{\"id\":7,\"someText\":\"" + UUID.randomUUID().toString() + "\"},{\"id\":8,\"someText\":\"" + UUID.randomUUID().toString() + "\"}]";
    }

    private String longJsonResponse() {
        return "[\n" +
                "  {\n" +
                "    \"_id\": \"" + System.currentTimeMillis() + "\",\n" +
                "    \"index\": 0,\n" +
                "    \"guid\": \"98f9a8c0-1715-447e-b89d-842a25157b78\",\n" +
                "    \"isActive\": false,\n" +
                "    \"balance\": \"$1,627.63\",\n" +
                "    \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 37,\n" +
                "    \"eyeColor\": \"brown\",\n" +
                "    \"name\": \"Graham Sanchez\",\n" +
                "    \"gender\": \"male\",\n" +
                "    \"company\": \"GENEKOM\",\n" +
                "    \"email\": \"grahamsanchez@genekom.com\",\n" +
                "    \"phone\": \"+1 (931) 484-2268\",\n" +
                "    \"address\": \"247 Borinquen Pl, Aguila, Montana, 2802\",\n" +
                "    \"about\": \"Laborum id qui eiusmod consequat amet magna voluptate veniam id. Elit proident laboris consectetur amet. Non do mollit in cillum excepteur nostrud in. Ad exercitation non anim sint adipisicing fugiat irure sint excepteur Lorem amet nostrud quis. Cillum nisi pariatur proident nisi est do occaecat minim anim tempor duis.\\r\\n\",\n" +
                "    \"registered\": \"2020-05-19T02:33:22 -02:00\",\n" +
                "    \"latitude\": 27.946368,\n" +
                "    \"longitude\": 45.30655,\n" +
                "    \"tags\": [\n" +
                "      \"in\",\n" +
                "      \"occaecat\",\n" +
                "      \"est\",\n" +
                "      \"minim\",\n" +
                "      \"dolore\",\n" +
                "      \"magna\",\n" +
                "      \"occaecat\"\n" +
                "    ],\n" +
                "    \"friends\": [\n" +
                "      {\n" +
                "        \"id\": 0,\n" +
                "        \"name\": \"Dickerson Bauer\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 1,\n" +
                "        \"name\": \"Lora Oconnor\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 2,\n" +
                "        \"name\": \"Brady Knight\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Graham Sanchez! You have 2 unread messages.\",\n" +
                "    \"favoriteFruit\": \"banana\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"_id\": \"605d04bfad0cee631b432037\",\n" +
                "    \"index\": 1,\n" +
                "    \"guid\": \"71d381be-8e0c-4ba4-9e22-a0b673b435e6\",\n" +
                "    \"isActive\": true,\n" +
                "    \"balance\": \"$3,070.42\",\n" +
                "    \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 27,\n" +
                "    \"eyeColor\": \"green\",\n" +
                "    \"name\": \"Dorsey Cooper\",\n" +
                "    \"gender\": \"male\",\n" +
                "    \"company\": \"BALUBA\",\n" +
                "    \"email\": \"dorseycooper@baluba.com\",\n" +
                "    \"phone\": \"+1 (906) 481-2748\",\n" +
                "    \"address\": \"483 Coffey Street, Loveland, Missouri, 358\",\n" +
                "    \"about\": \"Veniam sint adipisicing quis mollit anim velit sit exercitation qui ullamco cupidatat ipsum officia. Aliqua voluptate est labore dolor excepteur nulla non ullamco commodo esse reprehenderit reprehenderit ad exercitation. Amet amet irure aliquip officia. Labore incididunt cupidatat quis aliqua amet reprehenderit excepteur. Sit irure ad eiusmod duis eiusmod. Enim exercitation voluptate cillum dolor aliqua qui commodo in ea quis qui. Incididunt culpa ad excepteur cillum exercitation sit.\\r\\n\",\n" +
                "    \"registered\": \"2017-11-21T05:52:53 -01:00\",\n" +
                "    \"latitude\": 71.679143,\n" +
                "    \"longitude\": 102.069815,\n" +
                "    \"tags\": [\n" +
                "      \"in\",\n" +
                "      \"proident\",\n" +
                "      \"adipisicing\",\n" +
                "      \"qui\",\n" +
                "      \"elit\",\n" +
                "      \"Lorem\",\n" +
                "      \"Lorem\"\n" +
                "    ],\n" +
                "    \"friends\": [\n" +
                "      {\n" +
                "        \"id\": 0,\n" +
                "        \"name\": \"Pacheco Perry\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 1,\n" +
                "        \"name\": \"Carla Whitney\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 2,\n" +
                "        \"name\": \"Enid Dawson\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Dorsey Cooper! You have 4 unread messages.\",\n" +
                "    \"favoriteFruit\": \"strawberry\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"_id\": \"605d04bf2aab957c1875738b\",\n" +
                "    \"index\": 2,\n" +
                "    \"guid\": \"d1269c6c-f953-4717-a184-fbbbd76a53a8\",\n" +
                "    \"isActive\": true,\n" +
                "    \"balance\": \"$3,763.27\",\n" +
                "    \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 22,\n" +
                "    \"eyeColor\": \"blue\",\n" +
                "    \"name\": \"Nettie Moses\",\n" +
                "    \"gender\": \"female\",\n" +
                "    \"company\": \"GOLOGY\",\n" +
                "    \"email\": \"nettiemoses@gology.com\",\n" +
                "    \"phone\": \"+1 (999) 565-3024\",\n" +
                "    \"address\": \"556 Coleman Street, Hendersonville, Kansas, 5859\",\n" +
                "    \"about\": \"Proident culpa proident nisi deserunt enim culpa eiusmod mollit id. Est et sunt ullamco do non irure mollit aliquip cillum minim voluptate dolore velit. Nulla est aute ut fugiat duis. Duis cupidatat sint consequat proident. Minim commodo excepteur velit culpa laborum nulla. Ea officia esse irure ex tempor ut sunt eiusmod et eu nulla ullamco.\\r\\n\",\n" +
                "    \"registered\": \"2021-01-23T04:32:15 -01:00\",\n" +
                "    \"latitude\": 41.733675,\n" +
                "    \"longitude\": 138.518869,\n" +
                "    \"tags\": [\n" +
                "      \"tempor\",\n" +
                "      \"consequat\",\n" +
                "      \"sit\",\n" +
                "      \"culpa\",\n" +
                "      \"sunt\",\n" +
                "      \"dolore\",\n" +
                "      \"fugiat\"\n" +
                "    ],\n" +
                "    \"friends\": [\n" +
                "      {\n" +
                "        \"id\": 0,\n" +
                "        \"name\": \"Blackburn Alford\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 1,\n" +
                "        \"name\": \"Pennington Hudson\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 2,\n" +
                "        \"name\": \"Ila Evans\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Nettie Moses! You have 9 unread messages.\",\n" +
                "    \"favoriteFruit\": \"strawberry\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"_id\": \"605d04bf8e6788df0a4afdb5\",\n" +
                "    \"index\": 3,\n" +
                "    \"guid\": \"3a030a18-89eb-4f3c-b042-3d6db55dc5e9\",\n" +
                "    \"isActive\": true,\n" +
                "    \"balance\": \"$3,845.23\",\n" +
                "    \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 28,\n" +
                "    \"eyeColor\": \"brown\",\n" +
                "    \"name\": \"Perkins Riggs\",\n" +
                "    \"gender\": \"male\",\n" +
                "    \"company\": \"HARMONEY\",\n" +
                "    \"email\": \"perkinsriggs@harmoney.com\",\n" +
                "    \"phone\": \"+1 (986) 400-3166\",\n" +
                "    \"address\": \"724 Lee Avenue, Darlington, Georgia, 5677\",\n" +
                "    \"about\": \"Cupidatat deserunt et ipsum excepteur mollit tempor adipisicing dolor minim aliquip. Consectetur id veniam nulla officia occaecat consectetur magna exercitation esse id. Ad aute reprehenderit minim aliquip minim consequat laboris non anim velit anim nisi esse et. Est esse mollit dolore cillum nulla. Exercitation ea mollit occaecat enim quis fugiat voluptate anim id exercitation id. Anim incididunt reprehenderit reprehenderit fugiat deserunt. Fugiat ullamco voluptate officia ullamco est sunt in proident fugiat.\\r\\n\",\n" +
                "    \"registered\": \"2014-12-18T01:43:59 -01:00\",\n" +
                "    \"latitude\": -69.927037,\n" +
                "    \"longitude\": 49.980806,\n" +
                "    \"tags\": [\n" +
                "      \"tempor\",\n" +
                "      \"mollit\",\n" +
                "      \"nostrud\",\n" +
                "      \"esse\",\n" +
                "      \"officia\",\n" +
                "      \"cupidatat\",\n" +
                "      \"fugiat\"\n" +
                "    ],\n" +
                "    \"friends\": [\n" +
                "      {\n" +
                "        \"id\": 0,\n" +
                "        \"name\": \"Russo Giles\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 1,\n" +
                "        \"name\": \"Adeline Frank\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 2,\n" +
                "        \"name\": \"Foster Ross\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Perkins Riggs! You have 8 unread messages.\",\n" +
                "    \"favoriteFruit\": \"banana\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"_id\": \"605d04bf3b0b2d2eccc8205b\",\n" +
                "    \"index\": 4,\n" +
                "    \"guid\": \"17942a08-84ab-44d6-b2d2-49e1e4a5ecfd\",\n" +
                "    \"isActive\": false,\n" +
                "    \"balance\": \"$2,873.75\",\n" +
                "    \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 27,\n" +
                "    \"eyeColor\": \"brown\",\n" +
                "    \"name\": \"Adela Hewitt\",\n" +
                "    \"gender\": \"female\",\n" +
                "    \"company\": \"SYNKGEN\",\n" +
                "    \"email\": \"adelahewitt@synkgen.com\",\n" +
                "    \"phone\": \"+1 (984) 516-3612\",\n" +
                "    \"address\": \"718 Box Street, Hartsville/Hartley, Wyoming, 221\",\n" +
                "    \"about\": \"Occaecat qui sunt labore duis ut. Veniam aliquip ipsum deserunt ex voluptate dolor laboris commodo sunt excepteur dolor irure. Qui reprehenderit cillum labore dolore mollit deserunt et in. Fugiat minim ullamco sint nostrud et elit ullamco nisi sit. In mollit reprehenderit officia eu irure enim occaecat occaecat id sint occaecat reprehenderit. Reprehenderit adipisicing nostrud excepteur anim irure nulla laboris nostrud adipisicing veniam ipsum elit aliqua. Proident aliquip minim esse est aliqua laboris est officia ea consequat consectetur est non ad.\\r\\n\",\n" +
                "    \"registered\": \"2015-06-20T04:18:46 -02:00\",\n" +
                "    \"latitude\": 11.283114,\n" +
                "    \"longitude\": 154.905736,\n" +
                "    \"tags\": [\n" +
                "      \"laboris\",\n" +
                "      \"sint\",\n" +
                "      \"excepteur\",\n" +
                "      \"anim\",\n" +
                "      \"incididunt\",\n" +
                "      \"consectetur\",\n" +
                "      \"cupidatat\"\n" +
                "    ],\n" +
                "    \"friends\": [\n" +
                "      {\n" +
                "        \"id\": 0,\n" +
                "        \"name\": \"Johnston Wise\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 1,\n" +
                "        \"name\": \"Erika Spencer\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 2,\n" +
                "        \"name\": \"Hutchinson Pacheco\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Adela Hewitt! You have 7 unread messages.\",\n" +
                "    \"favoriteFruit\": \"apple\"\n" +
                "  }\n" +
                "]";
    }

}
