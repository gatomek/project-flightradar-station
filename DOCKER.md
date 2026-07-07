# Docker scripts

## Poland

### Building image
```sh
docker image build --file Dockerfile-poland --tag tegall1982/radar-poland:3 . --no-cache
```

### Publishing image
```sh
docker push tegall1982/radar-poland:3
```

## Germany

### Building image
```sh
docker image build --file Dockerfile-germany --tag tegall1982/radar-germany:3 . --no-cache
```

### Publishing image
```sh
docker push tegall1982/radar-germany:3
```

## Britain

### Building image
```sh
docker image build --file Dockerfile-britain --tag tegall1982/radar-britain:3 . --no-cache
```

### Publishing image
```sh
docker push tegall1982/radar-britain:3
```
