# Setup
## Kamatera
```sh
docker run --rm -v .:/project:ro -e ANSIBLE_CONFIG=/project/ansible/ansible.cfg -w /project/ansible gatomek_ansible ansible-playbook -i inventory/kamatera playbooks/setup.yml
```

## Tower
```sh
docker run --rm -it -v .:/project:ro -e ANSIBLE_CONFIG=/project/ansible/ansible.cfg -w /project/ansible gatomek_ansible ansible-playbook -i inventory/kamatera playbooks/setup.yml -k
```
