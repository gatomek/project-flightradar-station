# Ansible

## Version
Ansible 2.17.14

## Ansible commands

```
ansible-playbook -i inventory/kamatera playbooks/setup.yml
ansible-playbook -i inventory/tower playbooks/setup.yml -k
```

# Configuration
- `/usr/local/{{ app_name }}` - folder for binaries, working directory
- `/etc/local/{{ app_name }}` - configuration like envs.file, certs, keys
