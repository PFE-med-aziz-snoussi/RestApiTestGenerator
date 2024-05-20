package com.vermeg.restapitestgenerator.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostmanCollection {
    private Info info;
    private List<Item> item = new ArrayList<>();
    private Auth auth;

    private List<Event> event = new ArrayList<>();


    public List<Event> getEvent() {
        return event;
    }

    public void setEvent(List<Event> event) {
        this.event = event;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public List<Item> getItem() {
        return item;
    }

    public void setItem(List<Item> item) {
        this.item = item;
    }

    public static class Info {
        private String name;
        private String schema;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }
    }

    public static class Item {
        String name;
        Request request;
        List<Event> event = new ArrayList<>();

        List<Item> item ;

        public List<Item> getItems() {
            return item;
        }

        public void setItems(List<Item> items) {
            this.item = items;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Request getRequest() {
            return request;
        }

        public void setRequest(Request request) {
            this.request = request;
        }

        public List<Event> getEvent() {
            return event;
        }

        public void setEvent(List<Event> event) {
            this.event = event;
        }
    }

    public static class Request {
        private String url;
        private String method;

        private List<Map<String, String>> header;
        private RequestBody body; // Body
        private Auth auth; // Auth details

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<Map<String, String>> getHeader() {
            return header;
        }

        public void setHeader(List<Map<String, String>> header) {
            this.header = header;
        }

        public RequestBody getBody() {
            return body;
        }

        public void setBody(RequestBody body) {
            this.body = body;
        }

        public Auth getAuth() {
            return auth;
        }

        public void setAuth(Auth auth) {
            this.auth = auth;
        }
    }

    public static class Event {
        private String listen;
        private Script script;

        public String getListen() {
            return listen;
        }

        public void setListen(String listen) {
            this.listen = listen;
        }

        public Script getScript() {
            return script;
        }

        public void setScript(Script script) {
            this.script = script;
        }
    }

    public static class Script {
        private String type;
        private String[] exec;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String[] getExec() {
            return exec;
        }

        public void setExec(String[] exec) {
            this.exec = exec;
        }
    }

    public static class RequestBody {
        private String mode;
        private String raw;
        private Options options;
        private FileContent file;

        public FileContent getFile() {
            return file;
        }

        public void setFile(FileContent file) {
            this.file = file;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }

        public Options getOptions() {
            return options;
        }

        public void setOptions(Options options) {
            this.options = options;
        }
        public static class FileContent {
            private String src; // File source

            public FileContent() {
            }

            public String getSrc() {
                return src;
            }

            public void setSrc(String src) {
                this.src = src;
            }
        }

    }

    public static class Options {
        private RawOptions raw;

        public RawOptions getRaw() {
            return raw;
        }

        public void setRaw(RawOptions raw) {
            this.raw = raw;
        }
    }

    public static class RawOptions {
        private String language;

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    public static class Auth {
        String type;
        List<AuthKeyValue> apiKey;
        List<AuthKeyValue> basic;
        List<AuthKeyValue> bearer;
        List<AuthKeyValue> jwt;
        List<AuthKeyValue> oauth2;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<AuthKeyValue> getApiKey() {
            return apiKey;
        }

        public void setApiKey(List<AuthKeyValue> apiKey) {
            this.apiKey = apiKey;
        }

        public List<AuthKeyValue> getBasic() {
            return basic;
        }

        public void setBasic(List<AuthKeyValue> basic) {
            this.basic = basic;
        }

        public List<AuthKeyValue> getBearer() {
            return bearer;
        }

        public void setBearer(List<AuthKeyValue> bearer) {
            this.bearer = bearer;
        }

        public List<AuthKeyValue> getJwt() {
            return jwt;
        }

        public void setJwt(List<AuthKeyValue> jwt) {
            this.jwt = jwt;
        }

        public List<AuthKeyValue> getOauth2() {
            return oauth2;
        }

        public void setOauth2(List<AuthKeyValue> oauth2) {
            this.oauth2 = oauth2;
        }
    }

    public static class AuthKeyValue {
        private String key;
        private String value;
        private String type;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
