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
hold on
drawCircle(x(1),x(2),x(3));
hold off
x(3)


% used functions:
function [ n ] = calcNormUVec( a, b, direction )
    v = b - a; % calculating direction vector
    
    % calculate normal unit vector in dependence of direction
    if( direction > 0 )
        n = [ -v(2); v(1) ];
    else
        n = [ v(2); -v(1) ];
    end
    
    n = n ./ norm( n, 2 ); % normalize vector
end

function [ h ] = drawCircle(xx,yy,rr)
    th = 0:pi/50:2*pi;
    xunit = rr * cos(th) + xx;
    yunit = rr * sin(th) + yy;
    % plot circle
    h = plot(xunit, yunit);
    % plot center
    plot(xx, yy, 'rx');
end

