import requests
import xml
import xml.etree.ElementTree as ET


def get_version(full_version):
    return full_version[9:-9]


installations_request = requests.get("https://api.gbif.org/v1/installation?limit=1000&type=IPT_INSTALLATION")
installations_data = installations_request.json()

versions_usage = ({'timeout': 0, 'error_response_code': 0, 'parse_error': 0})

for installation in installations_data['results']:

    print("Trying {item}".format(item=installation['key']))
    endpoints = installation['endpoints']
    print("Number of endpoints: {quantity}".format(quantity=len(endpoints)))

    # Installation should have endpoints
    if len(endpoints) > 0:
        for endpoint in installation['endpoints']:
            endpoint_url = endpoint['url']

            try:
                # Set timeout to 10 seconds
                endpoint_data = requests.get(endpoint_url, timeout=4)

                # Process only requests with code 200
                if endpoint_data.status_code != 200:
                    print("GET {url} is {code}, skipping this one"
                          .format(url=endpoint_url, code=endpoint_data.status_code))
                    number_of_installed_with_wrong_code = versions_usage['error_response_code']
                    versions_usage['error_response_code'] = number_of_installed_with_wrong_code + 1
                    continue

                content = endpoint_data.content

                # Build xml tree of content
                try:
                    root = ET.fromstring(content)
                except xml.etree.ElementTree.ParseError:
                    print("Parse error")
                    number_of_installed_with_parse_error = versions_usage['parse_error']
                    versions_usage['parse_error'] = number_of_installed_with_parse_error + 1
                    continue

                # Iterate over tags to find one with the version
                for child in root.iter('*'):
                    if child.tag == 'generator':
                        version = get_version(child.text)

                        if version in versions_usage.keys():
                            number_of_installed = versions_usage[version]
                            versions_usage[version] = number_of_installed + 1
                        else:
                            versions_usage[version] = 1

                        print(version)
                        break
            except requests.exceptions.RequestException:
                print("Exception occurred (timeout probably)")
                number_of_installed_with_timeout = versions_usage['timeout']
                versions_usage['timeout'] = number_of_installed_with_timeout + 1
    else:
        print("Skip installation '{title}' with the key '{key}', no endpoints provided"
              .format(title=installation['title'], key=installation['key']))

sorted_versions_usage = sorted(versions_usage.items(), key=lambda kv: kv[0], reverse=True)

print("----------------")
print("Result:")

for item in sorted_versions_usage:
    print("'{i1}': {i2}".format(i1=item[0], i2=item[1]))
