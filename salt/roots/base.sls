base-pkgs:
    pkg.installed:
      - pkgs:
        - dos2unix
        - java-1.8.0-openjdk
        - java-1.8.0-openjdk-devel
        - redhat-lsb

iptables:
  service.dead:
    - enable: False
