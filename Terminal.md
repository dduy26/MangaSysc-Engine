cd ~/Truyen-Cloud

# 1. Show 4 containers đang chạy cô lập
docker compose ps

# 2. Kiểm tra Redis In-Memory phản hồi
docker exec -it truyencloud-redis redis-cli ping

# 3. Show log backend đang live
docker logs --tail 20 truyencloud-backend