from __future__ import annotations

import asyncio
from contextlib import asynccontextmanager
from random import choices
from string import ascii_letters, digits

import pytest
import pytest_asyncio
from httpx import AsyncClient, Response, codes

SAMPLES = ascii_letters + digits + "-_"

HOST = "localhost"
PORT = 8080


def random_string(length: int = 10) -> str:
    return "".join(choices(SAMPLES, k=length))


@pytest_asyncio.fixture
async def client():
    async with AsyncClient(base_url=f"http://{HOST}:{PORT}") as async_client:
        yield async_client


@pytest.mark.asyncio
async def test_get_routes(client: AsyncClient):
    resp = await client.get("/")
    assert resp.status_code == codes.OK

    ret = resp.json()
    assert ret["msg"] == "You got me :)"


@pytest.mark.asyncio
async def test_channel_create_query_delete(client: AsyncClient):
    channel = random_string()
    resp = await client.get(f"/create/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["created"] is True

    resp = await client.get(f"/status/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["status"] is True

    resp = await client.get(f"/delete/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["deleted"] is True


@pytest.mark.asyncio
async def test_list_channels__empty(client: AsyncClient):
    resp = await client.get("/list-channels")
    assert resp.status_code == codes.OK
    assert resp.json() == []


@pytest.mark.asyncio
async def test_list_channels__list(client: AsyncClient):
    channels = [random_string() for _ in range(10)]

    tasks = [client.get(f"/create/{c}") for c in channels]
    resps: list[Response] = await asyncio.gather(*tasks)
    for c, resp in zip(channels, resps):
        assert resp.status_code == codes.OK
        data = resp.json()
        assert data["channel"] == c
        assert data["created"] is True

    resp = await client.get("/list-channels")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert set(data) == set(channels)

    tasks = [client.get(f"/delete/{c}") for c in channels]
    resps = await asyncio.gather(*tasks)
    for c, resp in zip(channels, resps):
        assert resp.status_code == codes.OK
        data = resp.json()
        assert data["channel"] == c
        assert data["deleted"] is True


@pytest.mark.asyncio
async def test_channel_does_not_existed(client: AsyncClient):
    channel = random_string()

    # First create the channel
    resp = await client.get(f"/create/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["created"] is True
    # Then create it again
    resp = await client.get(f"/create/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["created"] is False

    # Second: delete the channel
    resp = await client.get(f"/delete/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["deleted"] is True
    # Then delete it again
    resp = await client.get(f"/delete/{channel}")
    assert resp.status_code == codes.NOT_FOUND
    data = resp.json()
    assert data["channel"] == channel
    assert data["deleted"] is False

    # Third: check the status of the channel (should be False)
    resp = await client.get(f"/status/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["status"] is False


@asynccontextmanager
async def auto_channel(client: AsyncClient, channel: str):
    # First create the channel
    resp = await client.get(f"/create/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["created"] is True

    yield

    # Finally: delete the channel
    resp = await client.get(f"/delete/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["deleted"] is True


async def handler(websocket, channel: str):
    await websocket.accept()
    while True:
        data = await websocket.receive_text()
        await websocket.send_text(data)


@pytest.mark.asyncio
async def test_channel_publish_and_consume(client: AsyncClient):
    async with auto_channel(client, channel := random_string()):
        ...
