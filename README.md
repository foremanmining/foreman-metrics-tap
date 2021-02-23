# foreman-metrics-tap

Taps metrics from an ASIC periodically and stores them to a file.

## To run:

```shell
java -jar metrics-tap-0.0.1-SNAPSHOT.jar --minerIp=192.168.1.189
```

### Configuration

The following additional command line arguments can be provided:

- `minerIp` sets the miner IP to query (**not defaulted, must be set**)
- `minerUsername`sets the username for the miner's management page (default:
  root)
- `minerPassword` sets the username for the miner's management page (default:
  root)
- `dumpFrequencyInMillis` sets how frequently to write the captured stats to the
  csv file (default: 10000)
- `queryFrequencyInMillis` sets how frequently to query the miner for stats (
  default: 1000)

Example complex configuration:

```shell
java -jar metrics-tap-0.0.1-SNAPSHOT.jar --minerIp=192.168.1.189 --minerUsername=my-username --minerPassword=my-password
```