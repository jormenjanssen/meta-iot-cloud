#!/bin/sh
build_root=$(cd "$(dirname "$0")" && pwd)
build_dir="$build_root"/build
output_name=modbus

rm -f "$build_dir"/"$output_name"
mkdir -p "$build_dir"

# Check for Azure IoT Edge headers
if [ ! -f /usr/include/azureiot/gateway.h ]; then
	echo Azure IoT Edge headers not found. Please make sure that the azure-iot-edge-dev package is installed.
	exit 1
fi

echo ---------- Building the Modbus IoT Edge sample ----------
gcc src/main.c --std=c99 -I/usr/include/azureiot -L. -lgateway -laziotsharedutil -o "$build_dir"/"$output_name"
[ $? -eq 0 ] || exit $?

cp -n src/*.json build/

echo Finished Successfully

echo Output can be found in "$build_dir"
