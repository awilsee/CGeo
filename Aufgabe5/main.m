% linear programming
clear all;

% reading data of file..
file = fopen('polygon.txt', 'r');
%file = fopen('testpolygon.txt', 'r');
polygon = fscanf(file, '%f %f', [2 inf]);
fclose(file);

% draw Polygon
figure;
fill(polygon(1, :), polygon(2, :), 'cyan');
axis equal;

% calculate direction of convex polygon with first three points
c = polygon(1:2,1:3)';
c = horzcat(c, [1;1;1]);
ccw = det(c);

% get size of polygon matrix, delete last line if its the same point as the first one
polygonSize = size(polygon, 2);
if (polygon(:,polygonSize) == polygon(:,1))
    polygon(:,polygonSize) = [];
    polygonSize = size(polygon, 2);
end

% Calculate normal unit vector for each of part of the polygon
% Through the ccw value the direction of the vector is always inner the polygon
normEVec = zeros(2, polygonSize);
for i = 1 : (polygonSize - 1)
    normEVec(:, i) = calcNormUVec(polygon(:, i), polygon(:, i+1), ccw);
end
normEVec(:, polygonSize) = calcNormUVec(polygon(:, polygonSize), polygon(:, 1), ccw);

%  calculate linear programming with general formula:
%  a11*x1 + a12*x1 + ... + a1n*xn <= b1
%  a21*x1 + a22*x1 + ... + a2n*xn <= b2
%   ...
%  am1*x1 + am2*x2 + ... + amn*xn <= bm

%  transfer the approach d*n >= r with d = m - a to the formula above:
%  - a1*n1 - a2*n2 >= - n1*m1 - n2*m2 + r
%   ...

b = zeros(polygonSize, 1);
for i = 1 : polygonSize
    b(i) = - polygon(1, i) * normEVec(1, i) - polygon(2, i) * normEVec(2, i);
end

%  get the negative transponend of the normal unit vector
A = -1 .* normEVec';
A( :, 3 ) = 1;  %set radius to 1

func = [ 0; 0; -1]; %maximum radius, so minimize -r

% finally calculating linprog
x =  linprog( func, A, b );

% plotting results:
%  plot circle
phi = 0:0.05:2*pi;
xk = (x(3) * cos(phi)) + x(1);
yk = (x(3) * sin(phi)) + x(2);

hold on;
plot(xk, yk, 'r');
%  plot center
plot(x(1), x(2), 'rx');