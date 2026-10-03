package com.codepath.backend;

import com.codepath.backend.model.Lesson;
import com.codepath.backend.model.Problem;
import com.codepath.backend.model.TestCase;
import com.codepath.backend.model.Topic;
import com.codepath.backend.repository.LessonRepository;
import com.codepath.backend.repository.ProblemRepository;
import com.codepath.backend.repository.TestCaseRepository;
import com.codepath.backend.repository.TopicRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public DataInitializer(
            TopicRepository topicRepository,
            LessonRepository lessonRepository,
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository
    ) {
        this.topicRepository = topicRepository;
        this.lessonRepository = lessonRepository;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Override
    public void run(String... args) {

        if (topicRepository.count() == 0) {
            createTopics();
        }

        if (lessonRepository.count() == 0) {
            createLessons();
        }

        if (problemRepository.count() == 0) {
            createProblems();
        }

        if (testCaseRepository.count() == 0) {
            createTestCases();
        }
    }

    private void createTopics() {

        topicRepository.save(new Topic(
                "Arrays",
                "Learn indexing, traversal, searching, insertion, deletion and array complexity.",
                "Beginner",
                1
        ));

        topicRepository.save(new Topic(
                "Strings",
                "Master string manipulation, frequency counting, searching and common patterns.",
                "Beginner",
                2
        ));

        topicRepository.save(new Topic(
                "Linked Lists",
                "Understand nodes, pointers, traversal, insertion, deletion and linked-list patterns.",
                "Intermediate",
                3
        ));

        topicRepository.save(new Topic(
                "Stacks & Queues",
                "Learn LIFO and FIFO data structures and their practical applications.",
                "Intermediate",
                4
        ));

        topicRepository.save(new Topic(
                "Recursion",
                "Build recursive thinking using base cases, recursive calls and classic problems.",
                "Intermediate",
                5
        ));

        topicRepository.save(new Topic(
                "Trees",
                "Learn binary trees, traversals, BSTs, recursion and tree-based problem solving.",
                "Intermediate",
                6
        ));

        topicRepository.save(new Topic(
                "Graphs",
                "Understand graph representation, BFS, DFS, shortest paths and traversal patterns.",
                "Advanced",
                7
        ));

        topicRepository.save(new Topic(
                "Dynamic Programming",
                "Learn overlapping subproblems, memoization, tabulation and classic DP problems.",
                "Advanced",
                8
        ));
    }

    private void createLessons() {

        Topic arrays =
                topicRepository.findAllByOrderByOrderNumberAsc().get(0);

        Topic strings =
                topicRepository.findAllByOrderByOrderNumberAsc().get(1);

        Topic linkedLists =
                topicRepository.findAllByOrderByOrderNumberAsc().get(2);

        Topic stacksQueues =
                topicRepository.findAllByOrderByOrderNumberAsc().get(3);

        Topic recursion =
                topicRepository.findAllByOrderByOrderNumberAsc().get(4);

        Topic trees =
                topicRepository.findAllByOrderByOrderNumberAsc().get(5);

        Topic graphs =
                topicRepository.findAllByOrderByOrderNumberAsc().get(6);

        Topic dp =
                topicRepository.findAllByOrderByOrderNumberAsc().get(7);


        // =========================================================
        // ARRAYS
        // =========================================================

        add(arrays, "Introduction to Arrays",
                """
                An array stores multiple values of the same type in contiguous memory.

                Example:
                int[] numbers = {10, 20, 30, 40};

                Each element has an index starting from 0. Therefore numbers[0] is 10
                and numbers[3] is 40.

                Arrays are useful when you need fast access to an element using its index.
                """, 1);

        add(arrays, "Array Traversal",
                """
                Traversal means visiting every element of an array.

                Example:
                for (int i = 0; i < numbers.length; i++) {
                    System.out.println(numbers[i]);
                }

                A single traversal usually takes O(n) time because every element is visited once.
                """, 2);

        add(arrays, "Searching in an Array",
                """
                Linear search checks elements one by one until the target is found.

                Example:
                int target = 30;

                for (int i = 0; i < numbers.length; i++) {
                    if (numbers[i] == target) {
                        System.out.println("Found at index " + i);
                        break;
                    }
                }

                Time complexity: O(n).
                Space complexity: O(1).
                """, 3);

        add(arrays, "Insertion and Deletion",
                """
                Inserting or deleting an element from the middle of an array may require
                shifting other elements.

                For example, inserting 25 between 20 and 30 requires 30 and later elements
                to move one position to the right.

                Average time complexity for middle insertion/deletion: O(n).
                """, 4);

        add(arrays, "Array Complexity",
                """
                Access by index is O(1).

                Traversal is O(n).
                Linear search is O(n).
                Insertion/deletion in the middle is O(n).

                Arrays are excellent when fast indexed access is more important than frequent
                insertion and deletion.
                """, 5);


        // =========================================================
        // STRINGS
        // =========================================================

        add(strings, "Introduction to Strings",
                """
                A string is a sequence of characters.

                Example:
                String name = "CodePath";

                Common operations include finding length, accessing characters, comparing
                strings and creating substrings.

                In Java, String objects are immutable, meaning their contents cannot be
                changed after creation.
                """, 1);

        add(strings, "String Traversal",
                """
                You can visit every character using a loop.

                Example:
                String s = "hello";

                for (int i = 0; i < s.length(); i++) {
                    System.out.println(s.charAt(i));
                }

                Traversing a string of length n takes O(n) time.
                """, 2);

        add(strings, "Character Frequency",
                """
                Frequency counting records how many times each character appears.

                For lowercase English letters, an integer array of size 26 can be used.

                Example idea:
                int[] freq = new int[26];
                freq[s.charAt(i) - 'a']++;

                This technique is useful in anagrams and duplicate-character problems.
                """, 3);

        add(strings, "Palindrome Strings",
                """
                A palindrome reads the same from left to right and right to left.

                Example:
                "madam" is a palindrome.

                A two-pointer approach compares the left and right characters and moves
                both pointers toward the center.

                Time complexity: O(n).
                Space complexity: O(1).
                """, 4);

        add(strings, "String Complexity",
                """
                Most basic string scans take O(n), where n is the string length.

                When repeatedly building strings, using StringBuilder can avoid unnecessary
                intermediate String objects.

                Example:
                StringBuilder result = new StringBuilder();
                result.append("Code");
                result.append("Path");
                """, 5);


        // =========================================================
        // LINKED LISTS
        // =========================================================

        add(linkedLists, "Introduction to Linked Lists",
                """
                A linked list consists of nodes. Each node stores data and a reference to
                another node.

                Basic node:
                class Node {
                    int data;
                    Node next;
                }

                Unlike arrays, linked-list elements do not need contiguous memory.
                """, 1);

        add(linkedLists, "Linked List Traversal",
                """
                Start from the head node and follow next references until null.

                Example:
                Node current = head;

                while (current != null) {
                    System.out.println(current.data);
                    current = current.next;
                }

                Traversal takes O(n) time.
                """, 2);

        add(linkedLists, "Insertion in Linked Lists",
                """
                Insertion can be efficient when the position or previous node is already
                known.

                To insert a new node after current:
                newNode.next = current.next;
                current.next = newNode;

                The pointer changes are O(1), excluding the time needed to find the position.
                """, 3);

        add(linkedLists, "Deletion in Linked Lists",
                """
                Deletion changes links so that the target node is skipped.

                For example:
                previous.next = current.next;

                Finding the node can take O(n), while changing the links itself is O(1).
                """, 4);

        add(linkedLists, "Reverse a Linked List",
                """
                An iterative reversal uses three references: previous, current and next.

                Core idea:
                next = current.next;
                current.next = previous;
                previous = current;
                current = next;

                Time complexity: O(n).
                Extra space: O(1).
                """, 5);


        // =========================================================
        // STACKS & QUEUES
        // =========================================================

        add(stacksQueues, "Introduction to Stacks",
                """
                A stack follows LIFO: Last In, First Out.

                Main operations:
                push - add an item
                pop - remove the top item
                peek - view the top item

                Stacks are used in function calls, undo operations and expression processing.
                """, 1);

        add(stacksQueues, "Stack Implementation",
                """
                A stack can be implemented using an array or Java's ArrayDeque.

                Example:
                ArrayDeque<Integer> stack = new ArrayDeque<>();
                stack.push(10);
                stack.push(20);
                int top = stack.pop();

                Push and pop are normally O(1).
                """, 2);

        add(stacksQueues, "Introduction to Queues",
                """
                A queue follows FIFO: First In, First Out.

                Main operations:
                enqueue - add at the rear
                dequeue - remove from the front

                Queues are useful in scheduling, buffering and breadth-first search.
                """, 3);

        add(stacksQueues, "Queue Implementation",
                """
                Java's ArrayDeque can be used as a queue.

                Example:
                ArrayDeque<Integer> queue = new ArrayDeque<>();
                queue.offer(10);
                queue.offer(20);
                int first = queue.poll();

                Adding and removing from the ends is O(1).
                """, 4);

        add(stacksQueues, "Stacks and Queues in DSA",
                """
                Choose a stack when the newest item should be processed first.

                Choose a queue when the oldest item should be processed first.

                Stack examples:
                undo operations, recursion, balanced parentheses.

                Queue examples:
                task scheduling, BFS and request processing.
                """, 5);


        // =========================================================
        // RECURSION
        // =========================================================

        add(recursion, "Introduction to Recursion",
                """
                Recursion occurs when a function calls itself.

                Every recursive solution needs:
                1. A base case.
                2. A recursive case that moves toward the base case.

                Without a valid base case, recursion can continue until stack overflow.
                """, 1);

        add(recursion, "Factorial Using Recursion",
                """
                Factorial is defined as:
                n! = n * (n - 1) * ... * 1

                Recursive definition:
                factorial(n) = n * factorial(n - 1)
                factorial(0) = 1

                The recursion depth is O(n).
                """, 2);

        add(recursion, "Fibonacci Recursion",
                """
                Fibonacci numbers follow:
                F(n) = F(n - 1) + F(n - 2)

                The direct recursive implementation repeats many subproblems and has
                exponential time complexity.

                This makes Fibonacci a useful example for understanding dynamic programming.
                """, 3);

        add(recursion, "Recursion and Call Stack",
                """
                Each recursive call creates a new stack frame.

                For:
                function(3)
                -> function(2)
                -> function(1)

                The calls return in reverse order.

                Understanding the call stack is essential for tracing recursive programs.
                """, 4);

        add(recursion, "Backtracking Basics",
                """
                Backtracking builds a solution step by step and reverses a choice when that
                choice cannot lead to a valid solution.

                Common examples include:
                N-Queens, permutations, combinations and maze problems.

                The general pattern is choose, explore, and undo.
                """, 5);


        // =========================================================
        // TREES
        // =========================================================

        add(trees, "Introduction to Trees",
                """
                A tree is a hierarchical data structure made of nodes and edges.

                Important terms:
                root, parent, child, leaf, depth and height.

                A binary tree allows each node to have at most two children.
                """, 1);

        add(trees, "Tree Traversals",
                """
                Three common depth-first traversals are:

                Preorder: Root, Left, Right
                Inorder: Left, Root, Right
                Postorder: Left, Right, Root

                Traversals can be implemented recursively or iteratively.
                """, 2);

        add(trees, "Binary Search Trees",
                """
                In a Binary Search Tree, values smaller than a node are placed in its left
                subtree and larger values are placed in its right subtree.

                Search, insertion and deletion can be O(log n) in a balanced BST, but can
                become O(n) when the tree is highly skewed.
                """, 3);

        add(trees, "Tree Height",
                """
                The height of a tree represents the longest path from the root toward a leaf,
                depending on the chosen height convention.

                A balanced tree has relatively small height, while a skewed tree can have
                height close to the number of nodes.
                """, 4);

        add(trees, "Tree Problem Solving",
                """
                Many tree problems can be solved recursively.

                A common pattern is:
                solve(left subtree)
                solve(right subtree)
                combine both results

                Always identify the information that each recursive call should return.
                """, 5);


        // =========================================================
        // GRAPHS
        // =========================================================

        add(graphs, "Introduction to Graphs",
                """
                A graph consists of vertices and edges.

                Graphs can be:
                directed or undirected,
                weighted or unweighted.

                Real-world examples include road networks, social networks and computer
                networks.
                """, 1);

        add(graphs, "Graph Representation",
                """
                Common representations include:

                Adjacency matrix:
                Uses O(V^2) space.

                Adjacency list:
                Stores neighboring vertices and usually uses O(V + E) space.

                Adjacency lists are commonly used for sparse graphs.
                """, 2);

        add(graphs, "Breadth First Search",
                """
                BFS explores a graph level by level using a queue.

                Basic process:
                1. Put the starting vertex into a queue.
                2. Mark it visited.
                3. Remove a vertex and visit its unvisited neighbors.
                4. Continue until the queue is empty.

                Time complexity with adjacency lists: O(V + E).
                """, 3);

        add(graphs, "Depth First Search",
                """
                DFS explores as far as possible before backtracking.

                It can be implemented using recursion or an explicit stack.

                With an adjacency-list representation, traversal takes O(V + E) time.
                """, 4);

        add(graphs, "Shortest Path Basics",
                """
                For unweighted graphs, BFS can find the shortest path in terms of number
                of edges.

                For weighted graphs with non-negative edge weights, Dijkstra's algorithm
                is a common approach.

                The correct algorithm depends on graph properties.
                """, 5);


        // =========================================================
        // DYNAMIC PROGRAMMING
        // =========================================================

        add(dp, "Introduction to Dynamic Programming",
                """
                Dynamic Programming solves problems by storing results of repeated
                subproblems.

                Two major ideas are:
                1. Overlapping subproblems.
                2. Optimal substructure.

                DP commonly reduces repeated computation.
                """, 1);

        add(dp, "Memoization",
                """
                Memoization is top-down dynamic programming.

                Start with a recursive solution and store the answer for each state so the
                same state is not solved repeatedly.

                Example:
                dp[n] = answer already calculated for state n.
                """, 2);

        add(dp, "Tabulation",
                """
                Tabulation is bottom-up dynamic programming.

                Instead of recursive calls, build a table starting from the smallest
                states and gradually calculate larger states.

                It often removes recursion overhead and makes the computation order explicit.
                """, 3);

        add(dp, "0/1 Knapsack",
                """
                In 0/1 Knapsack, each item can either be selected once or not selected.

                A common state is:
                dp[i][w] = maximum value using the first i items with capacity w.

                The transition considers taking or skipping the current item.
                """, 4);

        add(dp, "DP Problem-Solving Pattern",
                """
                A practical DP workflow is:

                1. Define the state.
                2. Identify the transition.
                3. Define base cases.
                4. Decide memoization or tabulation.
                5. Calculate complexity.
                6. Check whether the state can be space-optimized.

                Clear state definition is usually the hardest and most important step.
                """, 5);
    }


    // =========================================================
    // PROBLEMS
    // =========================================================

    private void createProblems() {

        addProblem(
                "Two Sum",
                "Easy",
                """
                Given an array of integers nums and an integer target,
                return the indices of the two numbers such that they add up to target.

                You may assume that each input has exactly one solution.
                """,
                """
                Input: nums = [2,7,11,15], target = 9
                Output: [0,1]

                Input: nums = [3,2,4], target = 6
                Output: [1,2]
                """,
                """
                2 <= nums.length <= 10^4
                -10^9 <= nums[i] <= 10^9
                -10^9 <= target <= 10^9
                """,
                """
                class Solution {
                    public int[] twoSum(int[] nums, int target) {

                        // Write your code here

                        return new int[]{};
                    }
                }
                """,
                1
        );


        addProblem(
                "Reverse an Array",
                "Easy",
                """
                Given an integer array, reverse the array in-place.

                The first element should become the last element,
                the second element should become the second-last element,
                and so on.
                """,
                """
                Input: [1,2,3,4,5]
                Output: [5,4,3,2,1]

                Input: [10,20,30]
                Output: [30,20,10]
                """,
                """
                1 <= array.length <= 10^5
                """,
                """
                class Solution {
                    public void reverse(int[] nums) {

                        // Write your code here

                    }
                }
                """,
                2
        );


        addProblem(
                "Find Maximum Element",
                "Easy",
                """
                Given an integer array, find and return the largest element.
                """,
                """
                Input: [3,7,2,9,4]
                Output: 9

                Input: [-5,-2,-10]
                Output: -2
                """,
                """
                1 <= nums.length <= 10^5
                """,
                """
                class Solution {
                    public int findMax(int[] nums) {

                        // Write your code here

                        return 0;
                    }
                }
                """,
                3
        );


        addProblem(
                "Check Palindrome",
                "Easy",
                """
                Given a string, determine whether it reads the same
                forward and backward.
                """,
                """
                Input: "madam"
                Output: true

                Input: "hello"
                Output: false
                """,
                """
                1 <= s.length() <= 10^5
                """,
                """
                class Solution {
                    public boolean isPalindrome(String s) {

                        // Write your code here

                        return false;
                    }
                }
                """,
                4
        );


        addProblem(
                "Valid Parentheses",
                "Easy",
                """
                Given a string containing the characters
                '(', ')', '{', '}', '[' and ']',
                determine whether the input string is valid.

                Every opening bracket must have the correct closing bracket.
                """,
                """
                Input: "()"
                Output: true

                Input: "()[]{}"
                Output: true

                Input: "(]"
                Output: false
                """,
                """
                1 <= s.length() <= 10^5
                """,
                """
                class Solution {
                    public boolean isValid(String s) {

                        // Write your code here

                        return false;
                    }
                }
                """,
                5
        );


        addProblem(
                "Reverse Linked List",
                "Easy",
                """
                Given the head of a singly linked list,
                reverse the linked list and return the new head.
                """,
                """
                Input: 1 -> 2 -> 3 -> 4
                Output: 4 -> 3 -> 2 -> 1
                """,
                """
                0 <= number of nodes <= 5000
                """,
                """
                class Solution {

                    static class ListNode {
                        int val;
                        ListNode next;

                        ListNode(int val) {
                            this.val = val;
                        }
                    }

                    public ListNode reverseList(ListNode head) {

                        // Write your code here

                        return null;
                    }
                }
                """,
                6
        );


        addProblem(
                "Binary Search",
                "Easy",
                """
                Given a sorted array of integers and a target value,
                return the index of target if it exists.
                Otherwise return -1.
                """,
                """
                Input: nums = [1,3,5,7,9], target = 7
                Output: 3

                Input: nums = [1,3,5,7,9], target = 4
                Output: -1
                """,
                """
                1 <= nums.length <= 10^5
                nums is sorted in ascending order.
                """,
                """
                class Solution {
                    public int search(int[] nums, int target) {

                        // Write your code here

                        return -1;
                    }
                }
                """,
                7
        );


        addProblem(
                "Maximum Subarray",
                "Medium",
                """
                Given an integer array, find the contiguous subarray
                with the largest sum and return its sum.
                """,
                """
                Input: [-2,1,-3,4,-1,2,1,-5,4]
                Output: 6

                Explanation:
                The subarray [4,-1,2,1] has the largest sum.
                """,
                """
                1 <= nums.length <= 10^5
                -10^4 <= nums[i] <= 10^4
                """,
                """
                class Solution {
                    public int maxSubArray(int[] nums) {

                        // Write your code here

                        return 0;
                    }
                }
                """,
                8
        );


        addProblem(
                "Merge Intervals",
                "Medium",
                """
                Given an array of intervals where intervals[i] = [start, end],
                merge all overlapping intervals.
                """,
                """
                Input: [[1,3],[2,6],[8,10],[15,18]]
                Output: [[1,6],[8,10],[15,18]]
                """,
                """
                1 <= intervals.length <= 10^4
                """,
                """
                class Solution {
                    public int[][] merge(int[][] intervals) {

                        // Write your code here

                        return new int[][]{};
                    }
                }
                """,
                9
        );


        addProblem(
                "Number of Islands",
                "Medium",
                """
                Given a grid containing '1' representing land and
                '0' representing water, count the number of islands.

                An island is surrounded by water and is formed by
                connecting adjacent lands horizontally or vertically.
                """,
                """
                Input:
                [
                  ["1","1","0","0"],
                  ["1","1","0","0"],
                  ["0","0","1","0"],
                  ["0","0","0","1"]
                ]

                Output: 3
                """,
                """
                1 <= rows, columns <= 300
                """,
                """
                class Solution {
                    public int numIslands(char[][] grid) {

                        // Write your code here

                        return 0;
                    }
                }
                """,
                10
        );
    }


    // =========================================================
    // TEST CASES
    // =========================================================

    private void createTestCases() {

        Problem twoSum =
                problemRepository
                        .findByTitle("Two Sum")
                        .orElseThrow();

        addTestCase(
                twoSum,
                "nums = [2,7,11,15], target = 9",
                "[0,1]",
                1
        );

        addTestCase(
                twoSum,
                "nums = [3,2,4], target = 6",
                "[1,2]",
                2
        );


        Problem reverseArray =
                problemRepository
                        .findByTitle("Reverse an Array")
                        .orElseThrow();

        addTestCase(
                reverseArray,
                "[1,2,3,4,5]",
                "[5,4,3,2,1]",
                1
        );

        addTestCase(
                reverseArray,
                "[10,20,30]",
                "[30,20,10]",
                2
        );


        Problem maximum =
                problemRepository
                        .findByTitle("Find Maximum Element")
                        .orElseThrow();

        addTestCase(
                maximum,
                "[3,7,2,9,4]",
                "9",
                1
        );

        addTestCase(
                maximum,
                "[-5,-2,-10]",
                "-2",
                2
        );


        Problem palindrome =
                problemRepository
                        .findByTitle("Check Palindrome")
                        .orElseThrow();

        addTestCase(
                palindrome,
                "\"madam\"",
                "true",
                1
        );

        addTestCase(
                palindrome,
                "\"hello\"",
                "false",
                2
        );


        Problem validParentheses =
                problemRepository
                        .findByTitle("Valid Parentheses")
                        .orElseThrow();

        addTestCase(
                validParentheses,
                "\"()\"",
                "true",
                1
        );

        addTestCase(
                validParentheses,
                "\"()[]{}\"",
                "true",
                2
        );

        addTestCase(
                validParentheses,
                "\"(]\"",
                "false",
                3
        );


        Problem reverseLinkedList =
                problemRepository
                        .findByTitle("Reverse Linked List")
                        .orElseThrow();

        addTestCase(
                reverseLinkedList,
                "1 -> 2 -> 3 -> 4",
                "4 -> 3 -> 2 -> 1",
                1
        );


        Problem binarySearch =
                problemRepository
                        .findByTitle("Binary Search")
                        .orElseThrow();

        addTestCase(
                binarySearch,
                "nums = [1,3,5,7,9], target = 7",
                "3",
                1
        );

        addTestCase(
                binarySearch,
                "nums = [1,3,5,7,9], target = 4",
                "-1",
                2
        );


        Problem maximumSubarray =
                problemRepository
                        .findByTitle("Maximum Subarray")
                        .orElseThrow();

        addTestCase(
                maximumSubarray,
                "[-2,1,-3,4,-1,2,1,-5,4]",
                "6",
                1
        );


        Problem mergeIntervals =
                problemRepository
                        .findByTitle("Merge Intervals")
                        .orElseThrow();

        addTestCase(
                mergeIntervals,
                "[[1,3],[2,6],[8,10],[15,18]]",
                "[[1,6],[8,10],[15,18]]",
                1
        );


        Problem numberOfIslands =
                problemRepository
                        .findByTitle("Number of Islands")
                        .orElseThrow();

        addTestCase(
                numberOfIslands,
                "[[1,1,0,0],[1,1,0,0],[0,0,1,0],[0,0,0,1]]",
                "3",
                1
        );
    }


    private void addProblem(
            String title,
            String difficulty,
            String description,
            String examples,
            String constraints,
            String starterCode,
            int orderNumber
    ) {

        problemRepository.save(
                new Problem(
                        title,
                        difficulty,
                        description,
                        examples,
                        constraints,
                        starterCode,
                        orderNumber
                )
        );
    }


    private void addTestCase(
            Problem problem,
            String input,
            String expectedOutput,
            int orderNumber
    ) {

        testCaseRepository.save(
                new TestCase(
                        problem,
                        input,
                        expectedOutput,
                        orderNumber
                )
        );
    }


    private void add(
            Topic topic,
            String title,
            String content,
            int orderNumber
    ) {

        lessonRepository.save(
                new Lesson(
                        topic,
                        title,
                        content,
                        orderNumber
                )
        );
    }
}