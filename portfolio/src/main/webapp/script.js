// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random personal fact to the page.
 */
function addRandomFact() {
  const facts = [
    'I have 2 siblings.',
    'My favorite color is blue.',
    'I enjoy outdoor activities.',
    'I skipped a grade.',
    'I was born in Massachusetts.',
  ];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * Cycles backwards to the previous background image.
 */
function prevBackground() {
  changeBackground(-1);
}

/**
 * Cycles forwards to the next background image.
 */
function nextBackground() {
  changeBackground(1);
}

let currentBackgroundIndex = 0;

// List of background images and their color themes.
const backgroundThemes = [
  {
    img: 'forest.jpg',
    headingColor: '#523029',
    headingBgColor: 'rgba(160,82,45,.1)',
  }, {
    img: 'ocean.jpg',
    headingColor: 'navy',
    headingBgColor: 'rgba(65,105,225,.1)',
  }, {
    img: 'mountain.jpg',
    headingColor: 'midnightblue',
    headingBgColor: 'rgba(0,128,128,.15)',
  }, {
    img: 'balloons.jpg',
    headingColor: 'indianred',
    headingBgColor: 'rgba(255,215,0,.2)',
  }, {
    img: 'iceberg.jpg',
    headingColor: 'darkslategray',
    headingBgColor: 'rgba(72,61,139,.2)',
  }, {
    img: 'grass.jpg',
    headingColor: 'green',
    headingBgColor: 'rgba(173,255,47,.2)',
  },
];

/**
 * Advances the background image by a given offset from the current image.
 */
function changeBackground(offset) {
  // Update the index of the current background image.
  currentBackgroundIndex = (currentBackgroundIndex + offset) % backgroundThemes.length;

  // Wrap around to the last image if the index is negative.
  if (currentBackgroundIndex < 0) {
    currentBackgroundIndex += backgroundThemes.length;
  }

  const newBackground = backgroundThemes[currentBackgroundIndex];

  // Update the page with the new background image.
  const html = document.documentElement;
  html.style.backgroundImage = `url(images/${newBackground.img})`;

  // Update the headings to the color theme for the new image.
  updateColorTheme(newBackground.headingColor, newBackground.headingBgColor);
}

/**
 * Updates the heading color CSS properties.
 */
function updateColorTheme(headingColor, headingBgColor) {
  document.body.style.setProperty('--heading-color', headingColor);
  document.body.style.setProperty('--heading-bg-color', headingBgColor);
}

/**
 * Fetches the list of comments from the server and adds them to the DOM.
 */
async function getComments() {
  const maxComments = document.getElementById('max-comments').value;
  const response = await fetch(`/data?max-comments=${maxComments}`);
  const comments = await response.json();

  const commentsContainer = document.getElementById('comments-container');
  commentsContainer.innerHTML = '';

  // Create <h4> and <p> elements for each comment's author and text.
  for (const comment of comments) {
    const commentElement = document.createElement('div');
    createTextElement(commentElement, 'h4', comment.author);
    createTextElement(commentElement, 'p', comment.commentText);
    commentsContainer.appendChild(commentElement);
  }
}

/**
 * Creates an html element containing the specified text and
 * inserts it as the last child of a given parent element.
 */
function createTextElement(parentElement, htmlTag, innerText) {
  const element = document.createElement(htmlTag);
  element.innerText = innerText;
  parentElement.appendChild(element);
  return element;
}

/**
 * Deletes all comments from the server and removes them from the page.
 */
async function deleteAllComments() {
  const response = await fetch('/delete-data', {
    method: 'POST'
  });
  
  getComments();
}