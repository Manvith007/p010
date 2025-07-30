def solve_bowling_balls():
    # Read number of test cases
    T = int(input())
    
    for _ in range(T):
        # Read N, X, Y
        N, X, Y = map(int, input().split())
        
        # Read the weights of bowling balls
        weights = list(map(int, input().split()))
        
        # Count balls that Chef can use (weight between X and Y inclusive)
        usable_balls = 0
        for weight in weights:
            if X <= weight <= Y:
                usable_balls += 1
        
        print(usable_balls)

# Run the solution
solve_bowling_balls()