# Brave Search SDK

Brave Search SDK is a Java library for interacting with the Brave Search API.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-langengine-bravesearch</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configuration

Set your Brave Search API key in your application properties:

```properties
brave_api_key=your_api_key_here
```

Alternatively, you can set it programmatically:

```java
System.setProperty("brave_api_key", "your_api_key_here");
```

## Usage

### Simple Search

```java
BraveClient client = new BraveClient();
SearchResponse response = client.search("your search query");
```

### Search with Count

```java
BraveClient client = new BraveClient();
SearchResponse response = client.search("your search query", 10);
```

### Advanced Search

```java
BraveClient client = new BraveClient();

SearchRequest request = new SearchRequest();
request.setQuery("your search query");
request.setCount(10);
request.setSafesearch("strict");
request.setCountry("us");

SearchResponse response = client.search(request);
```

## API Reference

### BraveClient

The main client class for interacting with the Brave Search API.

#### Constructors

- `BraveClient()` - Creates a client using the API key from configuration
- `BraveClient(String apiKey)` - Creates a client with a specific API key
- `BraveClient(String apiKey, OkHttpClient okHttpClient)` - Creates a client with a specific API key and custom HTTP client

#### Methods

- `search(String query)` - Performs a simple search
- `search(String query, int count)` - Performs a search with a specific result count
- `search(SearchRequest request)` - Performs an advanced search with various parameters

### SearchRequest

Represents a search request with the following parameters:

- `query` - The search query string
- `count` - The number of results to return (default: 10)
- `offset` - The offset for pagination
- `safesearch` - The safe search level ("off", "moderate", "strict")
- `country` - The country code for region-specific results
- `searchLang` - The language for the search query
- `uiLang` - The UI language for results
- `spellcheck` - Whether to perform spell checking (1 or 0)
- `resultFilter` - Filter for result types (e.g., "web", "news", "images")

### SearchResponse

Represents the search response with the following fields:

- `query` - Information about the query
- `results` - The list of search results
- `mixed` - Information about mixed content placement

## Exception Handling

All API errors are wrapped in `BraveException`. Handle it appropriately in your code:

```java
try {
    BraveClient client = new BraveClient();
    SearchResponse response = client.search("your query");
} catch (BraveException e) {
    System.err.println("Search failed: " + e.getMessage());
}
```