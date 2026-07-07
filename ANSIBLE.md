# Build Ansible Engine
```sh
docker image build --file Dockerfile-ansible --tag gatomek_ansible .
```

# Setup
## Kamatera
```sh
docker run --rm gatomek_ansible ansible-playbook -i inventory/kamatera playbooks/setup.yml
```

## Tower
```sh
docker run -it --rm gatomek_ansible ansible-playbook -i inventory/tower playbooks/setup.yml -k
```
