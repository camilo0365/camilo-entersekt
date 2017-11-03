# Assessment for Entersekt
> By Camilo Prieto Arciniegas (camilo.prieto93@gmail.com)

This project uses Spark, the Java minimalistic Web framework, to provide the requested API.

## How to build

```bash
$ docker build -t camilos-image .
```
**Note:** The initial build will take a while because we're installing Maven, Tini (see dockerfile comments) and 
all the project's and Maven's dependencies. It will pay off :)

## How to run the container

To run it with attached TTY (to see the logs, for instance):
```bash
$ docker run -i -t --rm --name camilos-container -p 8080:8080 camilos-image
```
**Note:** Ctrl-C will stop the server smoothly :)

To run it in detached mode:
```bash
$ docker run -d --rm --name camilos-container -p 8080:8080 camilos-image
```

## How to use

Make a `POST` request to [http://localhost:8080/inspect](http://localhost:8080/inspect) 
with `Content-Type: application/json` and a body as follows:
```json
{
  "path": "/the/path/to/the/directory"
}
```

Example using cURL:

```bash
$ curl -X POST \
    http://localhost:8080/inspect \
    -H 'content-type: application/json' \
    -d '{
  	"path" : "/usr/bin"
  }'
```

