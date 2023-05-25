import pytest

from httpx import AsyncClient
from httpx import codes

from string import ascii_letters, digits
from random import choices

SAMPLES = ascii_letters + digits + "-_"


def random_string(length: int = 10) -> str:
    return "".join(choices(SAMPLES, k=length))


@pytest.fixture
async def client():
    async with AsyncClient(base_url="http://localhost:8080") as async_client:
        yield async_client


@pytest.mark.asyncio
async def test_get_routes(client: AsyncClient):
    resp = await client.get("/")
    assert resp.status_code == codes.OK

    ret = resp.json()
    assert ret["msg"] == "You got me :)"


@pytest.mark.asyncio
async def test_list_channels(client: AsyncClient):
    resp = await client.get("/list-channels")
    assert resp.status_code == codes.OK
    assert resp.json() == []


@pytest.mark.asyncio
async def test_create_and_delete_channel(client: AsyncClient):
    channel = random_string()
    resp = await client.get(f"/create/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["created"] == True

    resp = await client.get(f"/status/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["status"] == True

    resp = await client.get(f"/delete/{channel}")
    assert resp.status_code == codes.OK
    data = resp.json()
    assert data["channel"] == channel
    assert data["deleted"] == True
