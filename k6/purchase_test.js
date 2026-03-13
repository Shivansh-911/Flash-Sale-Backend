import http from 'k6/http';
import { check, sleep } from 'k6';


export const options = {
    vus : 200,
    duration : '30s'
}


export default function () {
    
    const payload = JSON.stringify({
        "userId": crypto.randomUUID(),
        "productId": "b17d3d96-4bdc-4364-9d37-d2b29922f8b4",
        "quantity": 1,
        idempotencyKey: crypto.randomUUID().toString()
    });

    const params = {
        headers: {
            'Content-Type': 'application/json'
        }
    };

    const res = http.post('http://localhost:8080/api/purchase/', payload, params);

    check(res, {        
        'is status 200': (r) => r.status === 200
    });

}