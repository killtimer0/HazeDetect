import asyncio, socket, requests, logging
from enum import Enum
from datetime import datetime
from urllib.parse import urljoin
from flask import Flask, request

hostname = socket.gethostname()
ipaddr = socket.gethostbyname(hostname)
port = 8193

# author's API key
APPKEY = '70126ee8143d4e689c1ea1b571c5f1fe'
URL_BASE = 'https://devapi.qweather.com/v7/'
URL_WEATHER_NOW = urljoin(URL_BASE, 'weather/now')
URL_WEATHER_7D = urljoin(URL_BASE, 'weather/7d')
URL_AQI = urljoin(URL_BASE, 'air/now')
URL_GEOBASE = 'https://geoapi.qweather.com/v2/'
URL_CITY = urljoin(URL_GEOBASE, 'city/lookup')

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

app = Flask(__name__)
session = requests.Session()

def get(url: str, /, **kwargs) -> dict[str]:
    params = dict(key=APPKEY)
    params.update(kwargs)
    response = session.get(url, params=params)
    response.raise_for_status()
    return response.json()

async def get_weather_info_async(apis: list[tuple[str, dict[str]]]) -> list[dict[str]]:
    loop = asyncio.get_event_loop()
    responses = []
    for url, params in apis:
        data = await loop.run_in_executor(None, lambda: get(url, **params))
        if data['code'] != '200':
            raise RuntimeError(str(data))
        responses.append(data)
    return responses

class WeatherState(Enum):
    SUNNY = 0
    SUNNY_NIGHT = 1
    CLOUDY = 2
    CLOUDY_NIGHT = 3
    OVERCAST = 4
    OVERCAST_NIGHT = 5
    RAIN = 6
    RAIN_NIGHT = 7
    SNOW = 8
    SNOW_NIGHT = 9
    FOG = 10
    FOG_NIGHT = 11
    HAZE = 12
    HAZE_NIGHT = 13

def get_weather_background(date: datetime, sunrise: datetime | None, sunset: datetime | None, icon: int) -> WeatherState:
    def is_daytime() -> bool:
        if sunrise is None and sunset is None:
            return True
        elif sunrise is None and sunset is not None:
            return date < sunset
        elif sunrise is not None and sunset is None:
            return date >= sunrise
        else:
            return sunrise <= date < sunset

    match icon:
        case 100:
            return WeatherState.SUNNY
        case 150:
            return WeatherState.SUNNY_NIGHT
        case 101 | 102 | 103:
            return WeatherState.CLOUDY
        case 151 | 152 | 153:
            return WeatherState.CLOUDY_NIGHT
        case 104:
            return WeatherState.OVERCAST if is_daytime() else WeatherState.OVERCAST_NIGHT
        case 300 | 301:
            return WeatherState.RAIN
        case 350 | 351:
            return WeatherState.RAIN_NIGHT
        case 302 | 303 | 304 | 305 | 306 | 307 | 308 | 309 | 310 | 311 | 312 | 313 | 314 | 315 | 316 | 317 | 318 | 399:
            return WeatherState.RAIN if is_daytime() else WeatherState.RAIN_NIGHT
        case 400 | 401 | 402 | 403 | 404 | 405 | 408 | 409 | 410 | 499:
            return WeatherState.SNOW if is_daytime() else WeatherState.SNOW_NIGHT
        case 406 | 407:
            return WeatherState.SNOW
        case 456 | 457:
            return WeatherState.SNOW_NIGHT
        case 500 | 501 | 509 | 510 | 514 | 515:
            return WeatherState.FOG if is_daytime() else WeatherState.FOG_NIGHT
        case 502 | 503 | 504 | 507 | 508 | 511 | 512 | 513:
            return WeatherState.HAZE if is_daytime() else WeatherState.HAZE_NIGHT
        case _:
            return WeatherState.SUNNY if is_daytime() else WeatherState.SUNNY_NIGHT

def get_weather_info(location: str) -> dict[str]:
    date = datetime.now()
    apis = [
        (URL_WEATHER_NOW, dict(location=location)),
        (URL_WEATHER_7D, dict(location=location)),
        (URL_AQI, dict(location=location)),
    ]
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    result = loop.run_until_complete(get_weather_info_async(apis))
    weather_now, weather_7d, aqi = result
    tz = datetime.fromisoformat(weather_now['updateTime']).tzinfo
    icon = int(weather_now['now']['icon'])
    sunrise = sunset = None
    daily = weather_7d['daily']
    try:
        sunrise = datetime.strptime(daily[0]['sunrise'], '%H:%M').time()
        sunrise = datetime.combine(date, sunrise, date.tzinfo)
        sunset = datetime.strptime(daily[0]['sunset'], '%H:%M').time()
        sunset = datetime.combine(date, sunset, date.tzinfo)
    except ValueError:
        pass

    return {
        'currentDate': datetime.now(tz).isoformat(timespec='minutes'),
        'background': get_weather_background(date, sunrise, sunset, icon).value,
        'weatherInfo': {
            'link': weather_now['fxLink'],
            'updateTime': weather_now['updateTime'],
            **weather_now['now'],
        },
        'aqiInfo': {
            'link': aqi['fxLink'],
            'updateTime': aqi['updateTime'],
            **aqi['now'],
        },
        'dailyWeatherInfo': {
            'link': weather_7d['fxLink'],
            'updateTime': weather_7d['updateTime'],
            'data': daily,
        }
    }

@app.route('/haze_detect/city_from', methods=['GET'])
def get_city() -> dict:
    longitude = float(request.args['longitude'])
    latitude = float(request.args['latitude'])
    logger.info(f'{longitude=:.2f} {latitude=:.2f}')
    location = f'{longitude:.2f},{latitude:.2f}'
    data = get(URL_CITY, location=location)
    if data['code'] != '200':
        raise RuntimeError(str(data))
    location = data['location']
    if not location:
        return {}
    return dict(location=location[0])

@app.route('/haze_detect/city_search', methods=['GET'])
def search_city() -> dict:
    query = request.args['q'].strip()
    logger.info(f'{query=}')
    data = get(URL_CITY, location=query, number=20)
    if data['code'] != '200':
        return []
    return data['location']

@app.route('/haze_detect/weather', methods=['GET'])
def get_weather() -> dict:
    location = request.args['location']
    # with open('cache.py', encoding='utf-8') as f:
    #     data = eval(f.read())
    data = get_weather_info(location)
    logger.info(data)
    return data

app.run(host=ipaddr, port=port)
